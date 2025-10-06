package com.kryeit.telepost.storage;

import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.posts.Home;
import com.kryeit.telepost.posts.Post;
import com.kryeit.telepost.posts.Relation;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.jackson2.Jackson2Plugin;

import java.util.UUID;

public class Database {
    private static final Jdbi JDBI;
    private static final HikariDataSource dataSource;
    private static final boolean isMySQL;

    static {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(ConfigReader.DB_USER);
        hikariConfig.setPassword(ConfigReader.DB_PASSWORD);
        hikariConfig.setJdbcUrl(ConfigReader.DB_URL);

        isMySQL = ConfigReader.DB_URL.contains("mysql");

        try {
            dataSource = new HikariDataSource(hikariConfig);
            JDBI = Jdbi.create(new HikariDataSource(hikariConfig));
            JDBI.installPlugin(new Jackson2Plugin());

            if (isMySQL) {

                JDBI.registerRowMapper(Post.class, (rs, ctx) -> new Post(
                        rs.getLong("id"),
                        parseUUID(rs.getString("owner")),
                        rs.getString("name"),
                        rs.getInt("x"),
                        rs.getInt("z"),
                        rs.getBoolean("privated")
                ));

                JDBI.registerRowMapper(Home.class, (rs, ctx) -> new Home(
                        rs.getLong("id"),
                        parseUUID(rs.getString("player")),
                        rs.getLong("post_id")
                ));

                JDBI.registerRowMapper(Relation.class, (rs, ctx) -> new Relation(
                        rs.getLong("id"),
                        parseUUID(rs.getString("from_uuid")),
                        parseUUID(rs.getString("to_uuid")),
                        Relation.RelationType.valueOf(rs.getString("type"))
                ));
            } else {

                JDBI.registerRowMapper(Post.class, (rs, ctx) -> new Post(
                        rs.getLong("id"),
                        rs.getObject("owner", UUID.class),
                        rs.getString("name"),
                        rs.getInt("x"),
                        rs.getInt("z"),
                        rs.getBoolean("privated")
                ));

                JDBI.registerRowMapper(Home.class, (rs, ctx) -> new Home(
                        rs.getLong("id"),
                        rs.getObject("player", UUID.class),
                        rs.getLong("post_id")
                ));

                JDBI.registerRowMapper(Relation.class, (rs, ctx) -> new Relation(
                        rs.getLong("id"),
                        rs.getObject("from_uuid", UUID.class),
                        rs.getObject("to_uuid", UUID.class),
                        Relation.RelationType.valueOf(rs.getString("type"))
                ));
            }
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private static UUID parseUUID(String uuidString) {
        return uuidString != null ? UUID.fromString(uuidString) : null;
    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }

    public static boolean isMySQL() {
        return isMySQL;
    }
}