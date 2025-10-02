package com.kryeit.telepost;

import com.kryeit.telepost.beans.HomePost;
import com.kryeit.telepost.beans.NamedPost;
import com.kryeit.telepost.beans.Post;
import com.kryeit.telepost.config.StaticConfig;
import com.mojang.logging.LogUtils;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.jdbi.v3.core.Jdbi;
import org.jdbi.v3.core.mapper.reflect.ConstructorMapper;
import org.jdbi.v3.jackson2.Jackson2Plugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Database {
    private static final Logger logger = LoggerFactory.getLogger(Database.class);
    private static final Jdbi JDBI;
    private static final HikariDataSource dataSource;

    static {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setUsername(StaticConfig.dbUser);
        hikariConfig.setPassword(StaticConfig.dbPassword);
        hikariConfig.setJdbcUrl(StaticConfig.dbUrl);

        try {
            dataSource = new HikariDataSource(hikariConfig);
            JDBI = Jdbi.create(dataSource);
            JDBI.registerRowMapper(ConstructorMapper.factory(Post.class));
            JDBI.registerRowMapper(ConstructorMapper.factory(NamedPost.class));
            JDBI.registerRowMapper(ConstructorMapper.factory(HomePost.class));

            JDBI.installPlugin(new Jackson2Plugin());
        } catch (Exception e) {
            logger.error("Failed to initialize database connection", e);
            throw new ExceptionInInitializerError(e);
        }

    }

    public static Jdbi getJdbi() {
        return JDBI;
    }

    public static void closeDataSource() {
        LogUtils.getLogger().info("Closing database connection...");
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}