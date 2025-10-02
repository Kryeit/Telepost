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
                        privated BOOLEAN NOT NULL DEFAULT FALSE,
                        allowed JSONB NOT NULL DEFAULT '[]'
                    );
                    """);

            return null;
        });

        // Create homes table
        jdbi.withHandle(handle -> {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS homes (
                        id SERIAL PRIMARY KEY,
                        user UUID NOT NULL UNIQUE,
                        postId INT NOT NULL,
                        FOREIGN KEY (postId) REFERENCES posts(id) ON DELETE CASCADE
                    );
                    """);
            return null;
        });

    }



}
