package com.kryeit.telepost.config;

import com.kryeit.telepost.utils.JSONObject;

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
        JSONObject configObject = new JSONObject(config);

        // Database credentials
        DB_URL = configObject.getString("db-url");
        DB_USER = configObject.getString("db-user");
        DB_PASSWORD = configObject.getString("db-password");

        // Post system settings
        JSONObject postSystem = configObject.getObject("post-system");
        PostSystem.INNER_WORLDBORDER = Integer.parseInt(postSystem.getString("inner-worldborder"));
        PostSystem.OUTER_WORLDBORDER = Integer.parseInt(postSystem.getString("outer-worldborder"));
        PostSystem.INNER_POST_CLEARANCE = Integer.parseInt(postSystem.getString("inner-post-clearance"));
        PostSystem.OUTER_POST_CLEARANCE = Integer.parseInt(postSystem.getString("outer-post-clearance"));
        PostSystem.POST_WIDTH = Integer.parseInt(postSystem.getString("post-width"));

        // Other settings
        AUTO_NAMING = configObject.getBoolean("auto-naming");
        CLAIMBLOCKS_FOR_NAMING = Integer.parseInt(configObject.getString("claimblocks-for-naming"));

        // Post names
        if (configObject.has("next-post-names")) {
            var postNamesArray = configObject.getArray("next-post-names");
            for (int i = 0; i < postNamesArray.size(); i++) {
                POST_NAMES.add(postNamesArray.getString(i));
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