package com.kryeit.telepost.posts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kryeit.telepost.Config;
import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.storage.Database;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import org.jdbi.v3.core.mapper.reflect.ColumnName;
import org.jdbi.v3.json.Json;

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
                handle.createQuery("SELECT * FROM posts ORDER BY ((:worldX - x) * (:worldX - x) + (:worldZ - z) * (:worldZ - z)) ASC LIMIT 1")
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
                            "SELECT * FROM posts WHERE ((:x - x) * (:x - x) + (:z - z) * (:z - z)) < :maxGap * :maxGap"
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
                    Relation.RelationType relation = Relation.getRelation(owner, post.owner());

                    if (relation == Relation.RelationType.ALLY) {
                        requiredGap = GAP - leverage;
                    } else if (relation == Relation.RelationType.ENEMY) {
                        requiredGap = GAP + leverage;
                    } else {
                        requiredGap = GAP;
                    }
                }

                int dx = x - post.x();
                int dz = z - post.z();
                int distance = (int) Math.sqrt(dx * dx + dz * dz);

                if (distance < requiredGap) {
                    return "Post " + post.name() + " is " + distance + " blocks away, you need to be at least " + requiredGap + " blocks from it";
                }
            }

            handle.createUpdate("INSERT INTO posts (owner, name, x, z) VALUES (:owner, :name, :x, :z)")
                    .bind("owner", owner)
                    .bind("name", name)
                    .bind("x", x)
                    .bind("z", z)
                    .execute();

            PostBuilder.place(MinecraftServerSupplier.getServer().overworld(), "default", x, z);

            return null;
        });
    }

    public static void delete(String name) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("DELETE FROM posts WHERE name = :name")
                        .bind("name", name)
                        .execute()
        );
    }

    public static Post getByName(String name) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM posts WHERE name = :name")
                        .bind("name", name)
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

    public static List<Post> getVisible(UUID viewer) {
        List<Post> allPosts = Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM posts ORDER BY name")
                        .mapTo(Post.class)
                        .list()
        );

        return allPosts.stream()
                .filter(post -> {
                    if (post.owner() == null || post.owner().equals(viewer)) {
                        return true;
                    }

                    Relation.RelationType relation = Relation.getRelation(post.owner(), viewer);

                    if (relation == Relation.RelationType.ENEMY) {
                        return false;
                    }

                    if (relation == Relation.RelationType.ALLY) {
                        return true;
                    }

                    return !post.privated();
                })
                .toList();
    }

    public static class Leverage {

        private static int get(UUID player) {
            if (!ModList.get().isLoaded("luckperms")) {
                return ConfigReader.POST_GAP / 2;
            }

            LuckPerms luckPerms = LuckPermsProvider.get();
            var user = luckPerms.getUserManager().getUser(player);
            if (user == null) {
                return 0;
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
                    .orElse(0);
        }
    }

}
