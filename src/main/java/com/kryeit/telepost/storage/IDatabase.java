package com.kryeit.telepost.storage;

import com.kryeit.telepost.storage.bytes.Home;
import com.kryeit.telepost.storage.bytes.NamedPost;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface IDatabase {
    List<NamedPost> getNamedPosts();

    void addNamedPost(NamedPost post);

    void deleteNamedPost(String id);

    void setHome(UUID playerID, Home home);

    void stop();

    Optional<Home> getHome(UUID playerID);

    Optional<NamedPost> getNamedPost(String id);

    String dump();
}