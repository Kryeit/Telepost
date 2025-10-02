package com.kryeit.telepost.storage;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kryeit.telepost.Config;
import com.kryeit.telepost.posts.Post;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.jackson2.Jackson2Plugin;


public class Database {
    private static final Jdbi JDBI;

    static {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(Config.DB_USER);
        hikariConfig.setPassword(Config.DB_PASSWORD);
        hikariConfig.setJdbcUrl(Config.DB_URL);

        JDBI = Jdbi.create(new HikariDataSource(hikariConfig));
        JDBI.installPlugin(new Jackson2Plugin());

        JDBI.registerRowMapper(ConstructorMapper.factory(Post.class));
    }

    public static Jdbi getJdbi() {
        return JDBI;
    }
}