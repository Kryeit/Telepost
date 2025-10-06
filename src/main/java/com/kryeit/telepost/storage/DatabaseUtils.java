package com.kryeit.telepost.storage;

import com.kryeit.telepost.config.ConfigReader;
import org.jdbi.v3.core.Jdbi;

import static com.kryeit.telepost.storage.Database.isMySQL;

public class DatabaseUtils {

    public static void createTables() {
        Jdbi jdbi = Database.getJdbi();

        if (isMySQL()) {
            createTablesMySQL(jdbi);
        } else {
            createTablesPostgreSQL(jdbi);
        }
    }

    private static void createTablesPostgreSQL(Jdbi jdbi) {
        jdbi.withHandle(handle -> {
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

    private static void createTablesMySQL(Jdbi jdbi) {
        jdbi.withHandle(handle -> {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS posts (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        owner VARCHAR(36),
                        name VARCHAR(255) NOT NULL UNIQUE,
                        x INT NOT NULL,
                        z INT NOT NULL,
                        privated BOOLEAN NOT NULL DEFAULT FALSE
                    );
                    """);
            return null;
        });

        jdbi.withHandle(handle -> {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS homes (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        player VARCHAR(36) NOT NULL UNIQUE,
                        postId INT NOT NULL,
                        FOREIGN KEY (postId) REFERENCES posts(id) ON DELETE CASCADE
                    );
                    """);
            return null;
        });

        jdbi.withHandle(handle -> {
            handle.execute("""
                    CREATE TABLE IF NOT EXISTS relations (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        from_uuid VARCHAR(36) NOT NULL,
                        to_uuid VARCHAR(36) NOT NULL,
                        type VARCHAR(10) NOT NULL,
                        UNIQUE(from_uuid, to_uuid)
                    );
                    """);
            return null;
        });
    }
}