package com.kryeit.telepost.storage;

import org.mapdb.DB;
import org.mapdb.DBMaker;
import org.mapdb.Serializer;

import java.util.UUID;
import java.util.concurrent.ConcurrentMap;

import static com.kryeit.telepost.Telepost.ID;

public class NamedPostStorage {

    private ConcurrentMap<String, UUID> map;
    private DB db;

    public NamedPostStorage() {
        db = DBMaker
                .fileDB("mods/" + ID +"/db/player_posts.db")
                .fileMmapEnable()
                .make();

        map = db
                .hashMap("player_posts", Serializer.STRING, Serializer.UUID)
                .createOrOpen();
    }

    public void put(String postID, UUID playerID) {
        map.put(postID, playerID);
    }

    public void deleteElement(String postID) {
        map.remove(postID);
    }

    public boolean hasPlayer(UUID playerID) {
        return map.containsValue(playerID);
    }

    public UUID getPlayer(String postID) {
        return map.get(postID);
    }

    public void close() {
        db.close();
    }
}
