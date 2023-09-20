package com.kryeit.telepost;

import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import net.minecraft.server.network.ServerPlayerEntity;

public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
    }

    public static String getNameById(String id) {
        for (NamedPost namedPost : Telepost.getDB().getNamedPosts()) {
            if (namedPost.id().equals(id)) {
                return namedPost.name();
            }
        }
        return null;
    }
    public static void teleport(ServerPlayerEntity player, Post post) {
        player.teleport(post.getX(), post.getY(), post.getZ());

    }
}
