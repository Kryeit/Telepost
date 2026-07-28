package com.kryeit.telepost.db_migrator;

import com.kryeit.telepost.db_migrator.storage.LevelDBImpl;
import com.kryeit.telepost.db_migrator.storage.NamedPostStorage;
import com.kryeit.telepost.db_migrator.storage.bytes.HomePost;
import com.kryeit.telepost.db_migrator.storage.bytes.NamedPost;
import com.kryeit.telepost.db_migrator.storage.bytes.ReadableByteArray;
import org.iq80.leveldb.DBIterator;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.statement.PreparedBatch;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Jdbi jdbi = Jdbi.create("jdbc:postgresql://localhost:5433/telepost", "postgres", "password");
        LevelDBImpl database = new LevelDBImpl();
        NamedPostStorage namedPosts = new NamedPostStorage("world/telepost", "playerPosts.properties");

        jdbi.useHandle(h -> {
            PreparedBatch postsBatch = h.prepareBatch("""
                    INSERT INTO posts (owner, name, x, z, privated)
                    VALUES (:owner::uuid, :name, :x, :z, :private)
                    """);

            for (NamedPost post : database.getNamedPosts()) {
                postsBatch.bind("owner", namedPosts.getPlayerForPost(post.id()))
                        .bind("name", post.name())
                        .bind("x", (int) post.location().x())
                        .bind("z", (int) post.location().z())
                        .bind("private", post.isPrivate())
                        .add();
            }

            // ------------------------------------

            PreparedBatch homesBatch = h.prepareBatch("""
                    INSERT INTO homes (player, postid)
                    SELECT :player, id
                    FROM posts
                    WHERE x = :x
                      AND z = :z
                    LIMIT 1
                    """);

            try (DBIterator iterator = database.homesDB.iterator()) {
                for (iterator.seekToFirst(); iterator.hasNext(); iterator.next()) {
                    HomePost homePost = HomePost.fromBytes(new ReadableByteArray(iterator.peekNext().getValue()));

                    homesBatch.bind("x", (int) homePost.location().x())
                            .bind("z", (int) homePost.location().z())
                            .bind("player", homePost.playerID())
                            .add();
                }
            } catch (IOException ignored) {
            }

//            postsBatch.execute();
            homesBatch.execute();
        });
    }
}
