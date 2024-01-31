package com.kryeit.telepost.compat;

import com.flowpowered.math.vector.Vector3d;
import com.kryeit.telepost.post.Post;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.kryeit.telepost.post.Post.WORLD;

public class BlueMapImpl {
    private static final String MARKER_FILE_PATH = "mods/telepost/marker-file.json";
    public static MarkerSet markerSet = new MarkerSet("telepost-markers");

    static {
        loadMarkerSet();
    }

    public static void loadMarkerSet() {
        try {
            if (Files.exists(Paths.get(MARKER_FILE_PATH))) {
                try (FileReader reader = new FileReader(MARKER_FILE_PATH)) {
                    markerSet = MarkerGson.INSTANCE.fromJson(reader, MarkerSet.class);
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        if (markerSet == null) markerSet = new MarkerSet("telepost-markers");
        updateMarkerSet();
    }

    public static void updateMarkerSet() {
        BlueMapAPI.getInstance().flatMap(instance -> instance.getWorld(WORLD)).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put("telepost-markers", markerSet);
            }
        });
    }

    public static void createMarker(Post post, String name) {
        POIMarker marker = POIMarker.builder()
                .label(name)
                .position(new Vector3d(post.getX(), post.getY(), post.getZ()))
                .build();
        markerSet.getMarkers().put(name, marker);

        saveMarkerSet();
        updateMarkerSet();
    }

    public static void removeMarker(String name) {
        markerSet.getMarkers().remove(name);

        saveMarkerSet();
        updateMarkerSet();
    }

    private static void saveMarkerSet() {
        try (FileWriter writer = new FileWriter(MARKER_FILE_PATH)) {
            MarkerGson.INSTANCE.toJson(markerSet, writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
