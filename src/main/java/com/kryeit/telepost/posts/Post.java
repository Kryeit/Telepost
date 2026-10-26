package com.kryeit.telepost.posts;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.compat.BlueMapCompat;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.storage.Database;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.UUID;

public record Post(
        long id, UUID owner,
        String name,
        int x, int z,
        boolean privated
){

    public static final int GAP = ConfigReader.POST_GAP;
    public static final int DIAMETER = ConfigReader.POST_DIAMETER;

    public static Post getClosest(int worldX, int worldZ){
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM posts ORDER BY ((CAST(:worldX AS BIGINT) - x) * (CAST(:worldX AS BIGINT) - x) + (CAST(:worldZ AS BIGINT) - z) * (CAST(:worldZ AS BIGINT) - z)) LIMIT 1")
                        .bind("worldX", worldX)
                        .bind("worldZ", worldZ)
                        .mapTo(Post.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static void transfer(String name, UUID previousOwner, UUID newOwner) {
        int maxPosts = Utils.getMaxPosts(newOwner);

        int currentPosts = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM posts WHERE owner = :owner")
                        .bind("owner", newOwner)
                        .mapTo(Integer.class)
                        .one()
        );

        if (currentPosts >= maxPosts) {
            if (MinecraftServerSupplier.getServer() != null) {
                ServerPlayer player = MinecraftServerSupplier.getServer().getPlayerList().getPlayer(previousOwner);
                if (player != null) {
                    player.sendSystemMessage(Component.literal("The new owner of this post can only have " + maxPosts + " posts"));
                }
            }
            return;
        }

        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE posts SET owner = :newOwner WHERE name = :name AND owner = :previousOwner")
                        .bind("newOwner", newOwner)
                        .bind("previousOwner", previousOwner)
                        .bind("name", name)
                        .execute()
        );
    }

    public static void rename(String oldName, String newName) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("UPDATE posts SET name = :newName WHERE name = :oldName")
                        .bind("newName", newName)
                        .bind("oldName", oldName)
                        .execute()
        );
    }

    public static String create(UUID owner, String name, int x, int z) {
        int maxPosts = Utils.getMaxPosts(owner);

        int currentPosts = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM posts WHERE owner = :owner")
                        .bind("owner", owner)
                        .mapTo(Integer.class)
                        .one()
        );

        if (currentPosts >= maxPosts) {
            return "You can only have " + maxPosts + " posts";
        }

        int leverage = Leverage.get(owner);

        return Database.getJdbi().withHandle(handle -> {
            List<Post> nearbyPosts = handle.createQuery(
                            "SELECT * FROM posts WHERE ((CAST(:x AS BIGINT) - x) * (CAST(:x AS BIGINT) - x) + (CAST(:z AS BIGINT) - z) * (CAST(:z AS BIGINT) - z)) < CAST(:maxGap AS BIGINT) * :maxGap"
                    )
                    .bind("x", x)
                    .bind("z", z)
                    .bind("maxGap", GAP + leverage)
                    .mapTo(Post.class)
                    .list();

            for (Post post : nearbyPosts) {
                int requiredGap;

                if (post.owner() == null) {
                    requiredGap = GAP;
                } else {
                    Relation.RelationType mine = Relation.getRelation(owner, post.owner());
                    Relation.RelationType theirs = Relation.getRelation(post.owner(), owner);

                    if (mine == Relation.RelationType.ENEMY || theirs == Relation.RelationType.ENEMY) {
                        requiredGap = GAP + leverage;
                    } else if (mine == Relation.RelationType.ALLY || theirs == Relation.RelationType.ALLY) {
                        requiredGap = GAP - leverage;
                    } else {
                        requiredGap = GAP;
                    }
                }

                long dx = x - post.x();
                long dz = z - post.z();
                int distance = (int) Math.sqrt(dx * dx + dz * dz);

                if (distance < requiredGap) {
                    return "Post " + post.name() + " is " + distance + " blocks away, you need to be at least " + requiredGap + " blocks from it";
                }
            }

            Post post = handle.createQuery("""
                            INSERT INTO posts (owner, name, x, z)
                            VALUES (:owner, :name, :x, :z)
                            RETURNING *
                            """)
                    .bind("owner", owner)
                    .bind("name", name)
                    .bind("x", x)
                    .bind("z", z)
                    .mapTo(Post.class)
                    .one();

            PostBuilder.place(MinecraftServerSupplier.getServer().overworld(), "default", x, z);

            if (Utils.isCompatAddonAvailable("bluemap")) {
                BlueMapCompat.createMarker(post);
            }

            return null;
        });
    }

    public static void delete(String name) {
        int rowsChanged = Database.getJdbi().withHandle(h -> h.createUpdate("DELETE FROM posts WHERE name = :name")
                .bind("name", name)
                .execute());

        if (rowsChanged > 0 && Utils.isCompatAddonAvailable("bluemap")) {
            BlueMapCompat.removeMarker(name);
        }
    }

    public static Post getByName(String name) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM posts WHERE LOWER(name) = LOWER(:name) LIMIT 1")
                        .bind("name", name.trim())
                        .mapTo(Post.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static Post getById(long id) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM posts WHERE id = :id")
                        .bind("id", id)
                        .mapTo(Post.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static boolean togglePrivacy(String name) {
        return Database.getJdbi().withHandle(handle -> {
            handle.createUpdate("UPDATE posts SET privated = NOT privated WHERE name = :name")
                    .bind("name", name)
                    .execute();

            return handle.createQuery("SELECT privated FROM posts WHERE name = :name")
                    .bind("name", name)
                    .mapTo(Boolean.class)
                    .one();
        });
    }

    public static List<Post> getOwned(UUID owner) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM posts WHERE owner = :owner ORDER BY name")
                        .bind("owner", owner)
                        .mapTo(Post.class)
                        .list()
        );
    }

    public static List<Post> getAll() {
        return Database.getJdbi().withHandle(h -> h.createQuery("""
                        SELECT *
                        FROM posts
                        """)
                .mapTo(Post.class)
                .list());
    }

    // Same rules as Relation.getRelation(owner, viewer), resolved in a single query:
    // the owner's specific relation to the viewer wins, otherwise their `*` default.
    public static List<Post> getVisible(UUID viewer) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("""
                                SELECT p.* FROM posts p
                                LEFT JOIN relations r ON r.from_uuid = p.owner AND r.to_uuid = :viewer
                                LEFT JOIN relations d ON d.from_uuid = p.owner AND d.to_uuid = :everyone
                                WHERE p.owner IS NULL
                                   OR p.owner = :viewer
                                   OR COALESCE(r.type, d.type) = 'ALLY'
                                   OR (COALESCE(r.type, d.type) IS DISTINCT FROM 'ENEMY' AND NOT p.privated)
                                ORDER BY p.name
                                """)
                        .bind("viewer", viewer)
                        .bind("everyone", Relation.EVERYONE)
                        .mapTo(Post.class)
                        .list()
        );
    }

    public static class Leverage {

        private static int get(UUID player) {
            if (!ModList.get().isLoaded("luckperms")) {
                return ConfigReader.POST_GAP / 2;
            }

            LuckPerms luckPerms = LuckPermsProvider.get();
            var user = luckPerms.getUserManager().getUser(player);
            if (user == null) {
                return ConfigReader.POST_GAP / 2;
            }

            return user.getCachedData()
                    .getPermissionData()
                    .getPermissionMap()
                    .keySet()
                    .stream()
                    .filter(perm -> perm.startsWith("telepost.leverage."))
                    .mapToInt(perm -> {
                        try {
                            return Integer.parseInt(perm.substring("telepost.leverage.".length()));
                        } catch (NumberFormatException e) {
                            return 0;
                        }
                    })
                    .max()
                    .orElse(ConfigReader.POST_GAP / 2);
        }
    }

}
