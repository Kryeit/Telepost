package com.kryeit.telepost.storage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class PlayerNamedPosts {
    private File file;
    private Properties properties;

    public PlayerNamedPosts(String filePath) throws IOException {
        this.file = new File(filePath);
        if (!file.exists()) {
            file.createNewFile();
        }
        this.properties = new Properties();
        this.properties.load(new FileInputStream(file));
    }

    public HashMap<String, UUID> getHashMap() {
        HashMap<String, UUID> hashMap = new HashMap<>();
        for (String key : properties.stringPropertyNames()) {
            String value = properties.getProperty(key);
            hashMap.put(key, UUID.fromString(value));
        }
        return hashMap;
    }

    public void setHashMap(HashMap<String, UUID> hashMap) throws IOException {
        for (Map.Entry<String, UUID> entry : hashMap.entrySet()) {
            properties.setProperty(entry.getKey(), entry.getValue().toString());
        }
        properties.store(new FileOutputStream(file), null);
    }

    public void addElement(String name, UUID playerID) throws IOException {
        HashMap<String, UUID> hashMap = getHashMap();
        hashMap.put(name, playerID);
        setHashMap(hashMap);
    }

    public void deleteElement(String name) throws IOException {
        HashMap<String, UUID> hashMap = getHashMap();
        hashMap.remove(name);
        setHashMap(hashMap);
    }
}
