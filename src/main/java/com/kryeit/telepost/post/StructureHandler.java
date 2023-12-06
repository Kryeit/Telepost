package com.kryeit.telepost.post;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Objects;

public class StructureHandler {

    public static void createStructures() {
        File generatedStructuresDir = new File("world/generated/minecraft/structures/");
        if (!generatedStructuresDir.exists()) {
            generatedStructuresDir.mkdirs();
        }

        String structuresPath = "structures/";
        try {
            try (InputStream listStream = StructureHandler.class.getClassLoader().getResourceAsStream(structuresPath);
                 BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(listStream)))) {

                reader.lines()
                        .filter(line -> line.endsWith(".nbt"))
                        .forEach(structureName -> {
                            try (InputStream inputStream = StructureHandler.class.getClassLoader().getResourceAsStream(structuresPath + structureName)) {
                                if (inputStream != null) {
                                    Path destinationPath = generatedStructuresDir.toPath().resolve(structureName);

                                    if (!Files.exists(destinationPath)) {
                                        Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
                                    }
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        });
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}





