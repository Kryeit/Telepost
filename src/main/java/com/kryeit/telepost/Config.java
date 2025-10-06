package com.kryeit.telepost;

import com.kryeit.telepost.config.ConfigReader;

public class Config {
    public static final boolean production = false;

    public static final String DB_URL = production
            ? ConfigReader.DB_URL
            : "jdbc:postgresql://localhost:5432/servus";

    public static final String DB_USER = production
            ? ConfigReader.DB_USER
            : "postgres";

    public static final String DB_PASSWORD = production
            ? ConfigReader.DB_PASSWORD
            : "lel";

}
