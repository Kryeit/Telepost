package com.kryeit.telepost.compat;

import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.markers.MarkerSet;

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
}
