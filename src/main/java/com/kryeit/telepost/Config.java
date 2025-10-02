package com.kryeit.telepost;

public class Config {
    public static final boolean production = false;

    public static final String DB_URL = production
            ? "jdbc:postgresql://kryeit.com:5432/servus"
            : "jdbc:postgresql://localhost:5432/servus";

    public static final String DB_USER = production
            ? System.getenv("DB_USER")
            : "postgres";

    public static final String DB_PASSWORD = production
            ? System.getenv("DB_PASSWORD")
            : "lel";

}
