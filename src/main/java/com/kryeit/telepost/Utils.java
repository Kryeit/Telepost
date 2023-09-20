package com.kryeit.telepost;

import com.kryeit.telepost.post.Post;
import net.minecraft.server.network.ServerPlayerEntity;

public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
    }

    public static void teleport(ServerPlayerEntity player, Post post) {
        player.teleport(post.getX(), post.getY(), post.getZ());

    }
}
