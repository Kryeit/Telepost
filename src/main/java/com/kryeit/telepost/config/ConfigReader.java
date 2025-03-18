package com.kryeit.telepost.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ConfigReader {
    // Database credentials
    public static String DB_URL;
    public static String DB_USER;
    public static String DB_PASSWORD;

    // Post system settings
    public static class PostSystem {
        public static int INNER_WORLDBORDER;
        public static int OUTER_WORLDBORDER;
        public static int INNER_POST_CLEARANCE;
        public static int OUTER_POST_CLEARANCE;
        public static int POST_WIDTH;
        public static String OVERWORLD;
    }

    // Other settings
    public static boolean AUTO_NAMING;
    public static int CLAIMBLOCKS_FOR_NAMING;
    public static List<String> POST_NAMES = new ArrayList<>();

    private ConfigReader() {}

    public static void readFile(Path path) throws IOException {
        String config = readOrCopyFile(path.resolve("config.json"), "/config.json");
        JsonObject configObject = JsonParser.parseString(config).getAsJsonObject();

        DB_URL = configObject.get("db-url").getAsString();
        DB_USER = configObject.get("db-user").getAsString();
        DB_PASSWORD = configObject.get("db-password").getAsString();

        // Post system settings
        JsonObject postSystem = configObject.getAsJsonObject("post-system");
        PostSystem.INNER_WORLDBORDER = Integer.parseInt(postSystem.get("inner-worldborder").getAsString());
        PostSystem.OUTER_WORLDBORDER = Integer.parseInt(postSystem.get("outer-worldborder").getAsString());
        PostSystem.INNER_POST_CLEARANCE = Integer.parseInt(postSystem.get("inner-post-clearance").getAsString());
        PostSystem.OUTER_POST_CLEARANCE = Integer.parseInt(postSystem.get("outer-post-clearance").getAsString());
        PostSystem.POST_WIDTH = Integer.parseInt(postSystem.get("post-width").getAsString());

        // Other settings
        AUTO_NAMING = configObject.get("auto-naming").getAsBoolean();
        CLAIMBLOCKS_FOR_NAMING = Integer.parseInt(configObject.get("claimblocks-for-naming").getAsString());

        // Post names
        if (configObject.has("next-post-names")) {

            POST_NAMES = new ArrayList<>(configObject.getAsJsonArray("next-post-names").size());
            for (var element : configObject.getAsJsonArray("next-post-names")) {
                POST_NAMES.add(element.getAsString());
            }
        }
    }

    public static String readOrCopyFile(Path path, String exampleFile) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            InputStream stream = ConfigReader.class.getResourceAsStream(exampleFile);
            if (stream == null) throw new NullPointerException("Cannot load example file");

            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            Files.copy(stream, path);
        }
        return Files.readString(path);
    }
}