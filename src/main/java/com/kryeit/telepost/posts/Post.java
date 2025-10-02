package com.kryeit.telepost.posts;

import com.kryeit.telepost.Config;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.storage.Database;

import java.util.List;
import java.util.UUID;

public record Post(
        long id, UUID owner,
        String name,
        int x, int z,
        boolean privated, List<UUID> allowed
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

    public static boolean create(UUID owner, String name, int x, int z) {
        return Database.getJdbi().withHandle(handle -> {
            boolean tooClose = handle.createQuery("SELECT EXISTS(SELECT 1 FROM posts WHERE ((:x - x) * (:x - x) + (:z - z) * (:z - z)) < :gap * :gap)")
                    .bind("x", x)
                    .bind("z", z)
                    .bind("gap", GAP)
                    .mapTo(Boolean.class)
                    .one();

            if (tooClose) return false;

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

    public static void addTrust(String name, UUID player) {
        // Implementation depends on how you store the allowed list
        // If using JSON array or separate table
    }

}
