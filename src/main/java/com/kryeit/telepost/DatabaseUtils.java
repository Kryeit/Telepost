package com.kryeit.telepost;

import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.post.GridIterator;
import org.jdbi.v3.core.Jdbi;

import java.util.Set;
import java.util.stream.Collectors;

public class DatabaseUtils {

    public static void createTables() {
        Jdbi jdbi = Database.getJdbi();

        jdbi.withHandle(handle -> {
            // Create Post table
            handle.execute("""
                        CREATE TABLE IF NOT EXISTS posts (
                            id BIGSERIAL PRIMARY KEY,
                            x BIGINT,
                            z BIGINT
                        )
                    """);

            // Create HomePost table
            handle.execute("""
                        CREATE TABLE IF NOT EXISTS homes (
                            player UUID PRIMARY KEY,
                            post_id BIGINT,
                            FOREIGN KEY (post_id) REFERENCES posts(id)
                        )
                    """);

            // Create NamedPost table
            handle.execute("""
                        CREATE TABLE IF NOT EXISTS named (
                            id BIGINT PRIMARY KEY,
                            name VARCHAR(255),
                            privated BOOLEAN,
                            player UUID,
                            is_admin BOOLEAN
                        )
                    """);

            return null;
        });

        populateInner();
    }

    public static void populateInner() {
        GridIterator iterator = new GridIterator();
        Jdbi jdbi = Database.getJdbi();

        long innerWorldborderSize = ConfigReader.PostSystem.INNER_WORLDBORDER;

        jdbi.useTransaction(handle -> {
            // Get existing coordinates as strings
            Set<String> existingPoints = handle.createQuery("SELECT x, z FROM posts")
                    .map((rs, ctx) -> rs.getLong("x") + "," + rs.getLong("z"))
                    .collect(Collectors.toSet());

            // Insert new points
            while (iterator.hasNext()) {
                long x = (long) iterator.next().x;
                long z = (long) iterator.next().z;
                String coords = x + "," + z;

                if (!existingPoints.contains(coords) &&
                        Math.abs(x) <= innerWorldborderSize &&
                        Math.abs(z) <= innerWorldborderSize) {

                    handle.execute("INSERT INTO posts (x, z) VALUES (?, ?)", x, z);
                }
            }
        });
    }
}