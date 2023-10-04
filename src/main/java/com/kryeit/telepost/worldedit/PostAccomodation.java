package com.kryeit.telepost.worldedit;

import com.kryeit.telepost.Utils;
import com.kryeit.telepost.post.Post;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.kryeit.telepost.post.Post.WIDTH;

public class PostAccomodation {
    public static void accomodate(Post post, ServerPlayerEntity player) {
        int halfWidth = ((WIDTH - 1) / 2) + 10;
        int x = post.getX();
        int y = post.getY();
        int z = post.getZ();

        Utils.runCommand("/pos1 " + (x + halfWidth) + "," + (y + 100) + "," + (z + halfWidth), player.getCommandSource());
        Utils.runCommand("/pos2 " + (x - halfWidth) + "," + y + "," + (z - halfWidth), player.getCommandSource());
        Utils.runCommand("/cut", player.getCommandSource());
        Utils.runCommand("/pos1 " + (x + halfWidth) + "," + y + "," + (z + halfWidth), player.getCommandSource());
        Utils.runCommand("/pos2 " + (x - halfWidth) + "," + (y - 10) + "," + (z - halfWidth), player.getCommandSource());
        Utils.runCommand("/deform y/=(exp(-z^2)*exp(-x^2))*0.01+2", player.getCommandSource());
        Utils.runCommand("/outset 12", player.getCommandSource());
        Utils.runCommand("/replace ##minecraft:leaves air", player.getCommandSource());
        Utils.runCommand("/replace ##minecraft:logs air", player.getCommandSource());
        Utils.runCommand("/smooth 4", player.getCommandSource());
    }
}
