package com.kryeit.telepost.beans;

import java.util.UUID;

public record HomePost(UUID player, long postId) {

    public Post asPost() {
        return PostApi.jdbi.withHandle(handle -> handle.createQuery("SELECT * FROM posts WHERE id = :id")
                .bind("id", postId)
                .mapToBean(Post.class)
                .findFirst()
                .orElseThrow());
    }
}
