package com.kryeit.telepost.compat;

import com.flowpowered.math.vector.Vector3d;
import com.kryeit.telepost.beans.NamedPost;
import com.kryeit.telepost.beans.PostApi;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

public class BlueMapImpl {
    public static final MarkerSet markerSet = new MarkerSet("posts");

    public static void loadMarkerSet() {
        for (NamedPost post : PostApi.NamedPostApi.get()) {
            if (post.privated())
                continue;

            createMarker(post, post.name());
        }
    }

    public static void createMarker(NamedPost namedPost, String name) {
        POIMarker marker = POIMarker.builder()
                .label(name)
                .position(new Vector3d(namedPost.asPost().x(), namedPost.asPost().y(), namedPost.asPost().z()))
                .build();
        markerSet.getMarkers().put(name, marker);
    }

    public static void removeMarker(String name) {
        markerSet.getMarkers().remove(name);
    }

}
