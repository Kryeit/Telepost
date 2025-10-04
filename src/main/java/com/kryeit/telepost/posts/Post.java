package com.kryeit.telepost.posts;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kryeit.telepost.Config;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.storage.Database;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
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

    public static boolean create(UUID owner, String name, int x, int z) {
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
                int distanceSquared = dx * dx + dz * dz;

                if (distanceSquared < requiredGap * requiredGap) {
                    return false;
                }
            }

            handle.createUpdate("INSERT INTO posts (owner, name, x, z) VALUES (:owner, :name, :x, :z)")
                    .bind("owner", owner)
                    .bind("name", name)
                    .bind("x", x)
                    .bind("z", z)
                    .execute();

            return true;
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
