package com.kryeit.telepost.compat;

import com.flowpowered.math.vector.Vector3d;
import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.posts.Post;
import com.kryeit.telepost.posts.PostBuilder;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import de.bluecolored.bluemap.api.markers.POIMarker;

public class BlueMapCompat {
    private static final MarkerSet MARKER_SET = new MarkerSet("posts");

    public static void loadMarkerSet() {
        for (Post post : Post.getAll()) {
            createMarker(post);
        }
    }

    public static void createMarker(Post post) {
        String name = post.name();
        double y = PostBuilder.getSolidHeight(MinecraftServerSupplier.getServer().overworld(), post.x(), post.z());

        POIMarker marker = POIMarker.builder()
                .label(name)
                .position(new Vector3d(post.x(), y, post.z()))
                .build();
        MARKER_SET.getMarkers().put(name, marker);
    }

    public static void removeMarker(String name) {
        MARKER_SET.getMarkers().remove(name);
    }


}
