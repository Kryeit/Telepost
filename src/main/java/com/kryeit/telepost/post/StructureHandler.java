package com.kryeit.telepost.post;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.*;
import java.util.Collections;
import java.util.stream.Stream;

public class StructureHandler {

    public static void createStructures() {
        File generatedStructuresDir = new File("world/generated/minecraft/structures/");
        if (!generatedStructuresDir.exists()) {
            generatedStructuresDir.mkdirs();
        }

        String structuresPath = "structures/";
        try {
            URI uri = StructureHandler.class.getResource("/" + structuresPath).toURI();

            FileSystem fileSystem;
            try {
                fileSystem = FileSystems.newFileSystem(uri, Collections.emptyMap());
            } catch (FileSystemAlreadyExistsException e) {
                fileSystem = FileSystems.getFileSystem(uri);
            }

            Path myPath = fileSystem.getPath(structuresPath);

            try (Stream<Path> walk = Files.walk(myPath, 1)) {
                walk.filter(p -> p.toString().endsWith(".nbt")).forEach(filePath -> {
                    try {
                        Path destination = generatedStructuresDir.toPath().resolve(filePath.getFileName().toString());
                        if (!Files.exists(destination)) {
                            Files.copy(filePath, destination, StandardCopyOption.COPY_ATTRIBUTES);
                        }
                    } catch (IOException e) {
                        System.err.println("Error copying file: " + e.getMessage());
                        e.printStackTrace();
                    }
                });
            }
        } catch (IOException | URISyntaxException e) {
            System.err.println("Exception occurred: " + e.getMessage());
            e.printStackTrace();
        }
    }
}







