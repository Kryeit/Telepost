package com.kryeit.telepost.compat;

import com.flowpowered.math.vector.Vector3d;
import com.kryeit.telepost.post.Post;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

import java.io.FileWriter;
import java.io.IOException;

import static com.kryeit.telepost.post.Post.WORLD;

public class BlueMapImpl {
    public static MarkerSet markerSet;

    public static void updateMarkerSet() {
        BlueMapAPI.getInstance().flatMap(instance -> instance.getWorld(WORLD)).ifPresent(world -> {
            for (BlueMapMap map : world.getMaps()) {
                map.getMarkerSets().put("posts", BlueMapImpl.markerSet);
            }
        });
    }

    public static void createMarker(Post post, String name) {
        POIMarker marker = POIMarker.builder()
                .label(name)
                .position(new Vector3d(post.getX(), post.getY(), post.getZ()))
                .build();
        BlueMapImpl.markerSet.put(name, marker);

        try (FileWriter writer = new FileWriter("marker-file.json")) {
            MarkerGson.INSTANCE.toJson(BlueMapImpl.markerSet, writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        BlueMapImpl.updateMarkerSet();
    }

    public static void removeMarker(String name) {
        // Remove the marker from the set
        BlueMapImpl.markerSet.remove(name);

        // Update the marker set file
        try (FileWriter writer = new FileWriter("mods/telepost/marker-file.json")) {
            MarkerGson.INSTANCE.toJson(BlueMapImpl.markerSet, writer);
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        // Update BlueMap
        BlueMapImpl.updateMarkerSet();
    }

}
