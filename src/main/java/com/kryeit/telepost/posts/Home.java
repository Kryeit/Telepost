package com.kryeit.telepost.posts;

import com.kryeit.telepost.storage.Database;

import java.util.UUID;

public record Home(
        long id, UUID player, long postId
) {

    public static Home getByUser(UUID player) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM homes WHERE player = :player")
                        .bind("player", Database.isMySQL() ? player.toString() : player)
                        .mapTo(Home.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static void setHome(UUID player, long postId) {
        if (Database.isMySQL()) {
            Database.getJdbi().useHandle(handle ->
                    handle.createUpdate("INSERT INTO homes (player, postId) VALUES (:player, :postId) ON DUPLICATE KEY UPDATE postId = :postId")
                            .bind("player", player.toString())
                            .bind("postId", postId)
                            .execute()
            );
        } else {
            Database.getJdbi().useHandle(handle ->
                    handle.createUpdate("INSERT INTO homes (player, postId) VALUES (:player, :postId) ON CONFLICT (player) DO UPDATE SET postId = :postId")
                            .bind("player", player)
                            .bind("postId", postId)
                            .execute()
            );
        }
    }
}