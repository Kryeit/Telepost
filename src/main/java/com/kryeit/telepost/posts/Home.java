package com.kryeit.telepost.posts;

import com.kryeit.telepost.storage.Database;

import java.util.UUID;

public record Home(
        long id, UUID user, long postId
) {

    public static Home getByUser(UUID user) {
        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT * FROM homes WHERE user = :user")
                        .bind("user", user)
                        .mapTo(Home.class)
                        .findOne()
                        .orElse(null)
        );
    }

    public static void setHome(UUID user, long postId) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("INSERT INTO homes (user, postId) VALUES (:user, :postId) ON CONFLICT (user) DO UPDATE SET postId = :postId")
                        .bind("user", user)
                        .bind("postId", postId)
                        .execute()
        );
    }
}
