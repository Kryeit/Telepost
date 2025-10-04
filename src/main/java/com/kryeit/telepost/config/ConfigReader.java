package com.kryeit.telepost.config;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigReader {

    public static int POST_GAP;
    public static int POST_RADIUS;

    private ConfigReader() {}

    public static void readFile(Path path) throws IOException {
        String config = readOrCopyFile(path.resolve("config.json"), "/config.json");

        JsonObject configObject = JsonParser.parseString(config).getAsJsonObject();

        POST_GAP = configObject.get("post-gap").getAsInt();
        POST_RADIUS = configObject.get("post-radius").getAsInt();
    }

    public static String readOrCopyFile(Path path, String exampleFile) throws IOException {
        File file = path.toFile();
        if (!file.exists()) {
            InputStream stream = ConfigReader.class.getResourceAsStream(exampleFile);
            if (stream == null) {
                throw new NullPointerException("Cannot load example file");
            }

            //noinspection ResultOfMethodCallIgnored
            file.getParentFile().mkdirs();
            Files.copy(stream, path);
        }

        return Files.readString(path);
    }
}