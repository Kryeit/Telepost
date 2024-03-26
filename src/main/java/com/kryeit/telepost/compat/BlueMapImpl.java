package com.kryeit.telepost.compat;

import com.flowpowered.math.vector.Vector3d;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.kryeit.telepost.utils.Utils;
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
    public static final MarkerSet markerSet = new MarkerSet("telepost-markers");

    public static void loadMarkerSet() {
        for (NamedPost post : Telepost.getDB().getNamedPosts()) {
            createMarker(post, post.name());
        }
    }

    public static void createMarker(NamedPost namedPost, String name) {
        Post post = new Post(namedPost);
        POIMarker marker = POIMarker.builder()
                .label(name)
                .position(new Vector3d(post.getX(), post.getY(), post.getZ()))
                .build();
        markerSet.getMarkers().put(name, marker);
    }

    public static void removeMarker(String name) {
        markerSet.getMarkers().remove(name);
    }

}
