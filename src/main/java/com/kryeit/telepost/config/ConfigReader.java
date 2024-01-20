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

    public static int GAP;
    public static int WIDTH;
    public static int WORLDBORDER;
    public static boolean AUTONAMING;
    public static List<String> POST_NAMES = new ArrayList<>();

    private ConfigReader() {

    }

    public static void readFile(Path path) throws IOException {
        String config = readOrCopyFile(path.resolve("config.json"), "/config.json");
        JSONObject configObject = new JSONObject(config);
        WIDTH = Integer.parseInt(configObject.getString("post-width"));
        GAP = Integer.parseInt(configObject.getString("post-gap"));
        WORLDBORDER = Integer.parseInt(configObject.getString("worldborder"));
        AUTONAMING = configObject.getBoolean("autonaming");

        if(configObject.has("next-post-names")) {
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
