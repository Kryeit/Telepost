package com.kryeit.telepost.beans;

import java.util.UUID;

public record NamedPost(long id, String name, boolean privated, UUID player, boolean isAdmin) {

    public Post asPost() {
        return PostApi.jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM posts WHERE id = :id")
                .bind("id", id)
                .mapToBean(Post.class)
                .findFirst()
                .orElseThrow());
    }

    public void togglePrivacy() {
        PostApi.jdbi.useHandle(handle -> handle.createUpdate("UPDATE named SET privated = :privated WHERE id = :id")
                .bind("privated", !privated)
                .bind("id", id)
                .execute());
    }
}
