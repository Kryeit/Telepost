package com.kryeit.telepost.storage;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class NamedPostStorage {

    private final File registryFile;
    private Map<String, UUID> postPlayerMap = new HashMap<>();

    public NamedPostStorage(String directory, String fileName) throws IOException {
        Files.createDirectories(Paths.get(directory)); // Ensure directory exists
        this.registryFile = new File(directory, fileName);
        loadRegistry();
    }

    private void loadRegistry() {
        if (!registryFile.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(registryFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("=", 2);
                if (parts.length == 2) {
                    postPlayerMap.put(parts[0], UUID.fromString(parts[1]));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void assignPostToPlayer(String postId, UUID playerUuid) {
        postPlayerMap.put(postId, playerUuid);
        saveRegistry();
    }

    public void revokePost(String postId) {
        postPlayerMap.remove(postId);
        saveRegistry();
    }

    public UUID getPlayerForPost(String postId) {
        return postPlayerMap.get(postId);
    }

    public boolean hasPost(String postId) {
        return postPlayerMap.containsKey(postId);
    }

    public boolean hasPlayer(UUID playerUuid) {
        return postPlayerMap.containsValue(playerUuid);
    }

    public void saveRegistry() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(registryFile, false))) {
            for (Map.Entry<String, UUID> entry : postPlayerMap.entrySet()) {
                writer.println(entry.getKey() + "=" + entry.getValue());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
