package com.kryeit.telepost.storage;

import com.kryeit.telepost.Config;
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

    static {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(Config.DB_USER);
        hikariConfig.setPassword(Config.DB_PASSWORD);
        hikariConfig.setJdbcUrl(Config.DB_URL);

        try {
            dataSource = new HikariDataSource(hikariConfig);
            JDBI = Jdbi.create(new HikariDataSource(hikariConfig));
            JDBI.installPlugin(new Jackson2Plugin());

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
                    rs.getObject("fromUUID", UUID.class),
                    rs.getObject("toUUID", UUID.class),
                    Relation.RelationType.valueOf(rs.getString("type"))
            ));
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }

    }

    public static void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }
}