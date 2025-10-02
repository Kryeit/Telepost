package com.kryeit.telepost.config;


import static com.kryeit.telepost.config.ConfigReader.*;

public class StaticConfig {
    public static final boolean production = true;

    public static final String dbUrl = production
            ? DB_URL
            : "jdbc:postgresql://localhost:5432/postgres";

    public static final String dbUser = production
            ? DB_USER
            : "postgres";
    public static final String dbPassword = production
            ? DB_PASSWORD
            : "lel";
}
