package com.kryeit.telepost.storage;

import org.jdbi.v3.core.Jdbi;

public class DatabaseUtils {
    public static void createTables() {
        Jdbi jdbi = Database.getJdbi();

        jdbi.withHandle(handle -> {

            // Create posts table
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS posts (
                        id SERIAL PRIMARY KEY,
                        owner UUID,
                        name VARCHAR(255) NOT NULL UNIQUE,
                        x INT NOT NULL,
                        z INT NOT NULL,
                        privated BOOLEAN NOT NULL DEFAULT FALSE
                    );
                    """);

            return null;
        });

        // Create homes table
        jdbi.withHandle(handle -> {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS homes (
                        id SERIAL PRIMARY KEY,
                        player UUID NOT NULL UNIQUE,
                        postId INT NOT NULL,
                        FOREIGN KEY (postId) REFERENCES posts(id) ON DELETE CASCADE
                    );
                    """);
            return null;
        });

        // Create relations table
        jdbi.withHandle(handle -> {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS relations (
                        id SERIAL PRIMARY KEY,
                        from_uuid UUID NOT NULL,
                        to_uuid UUID NOT NULL,
                        type VARCHAR(10) NOT NULL,
                        UNIQUE(from_uuid, to_uuid)
                    );
                    """);
            return null;
        });

    }



}
