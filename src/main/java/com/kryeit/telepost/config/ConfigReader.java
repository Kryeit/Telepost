package com.kryeit.telepost.config;

import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.utils.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConfigReader {

    private ConfigReader() {

    }

    public static void readFile(Path path) throws IOException {
        String config = readOrCopyFile(path.resolve("config.json"), "/example_config.json");
        JSONObject configObject = new JSONObject(config);
        Post.WIDTH = Integer.parseInt(configObject.getString("post-width"));
        Post.GAP = Integer.parseInt(configObject.getString("post-gap"));
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
