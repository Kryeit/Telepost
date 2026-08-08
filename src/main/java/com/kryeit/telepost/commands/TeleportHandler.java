package com.kryeit.telepost.commands;

import com.kryeit.telepost.posts.Home;
import com.kryeit.telepost.posts.Post;
import com.kryeit.telepost.posts.PostBuilder;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;

public class TeleportHandler {

    public static boolean visit(ServerPlayer player, String postName) {
        if (!isNearPost(player)) {
            player.sendSystemMessage(Component.literal("You need to be closer to a post"));
            return false;
        }

        Post post = Post.getByName(postName);
        if (post == null) {
            return false;
        }

        teleportToPost(player, post);
        return true;
    }

    public static boolean home(ServerPlayer player) {
        if (!isNearPost(player)) {
            player.sendSystemMessage(Component.literal("You need to be closer to a post"));
            return false;
        }

        Home home = Home.getByUser(player.getUUID());
        if (home == null) {
            return false;
        }

        Post post = Post.getById(home.postId());
        if (post == null) {
            return false;
        }

        teleportToPost(player, post);
        return true;
    }

    private static void teleportToPost(ServerPlayer player, Post post) {
        ServerLevel level = player.serverLevel();
        double y = PostBuilder.getSolidHeight(level, post.x(), post.z());

        player.teleportTo(level, post.x() + 0.5, y, post.z() + 0.5, player.getYRot(), player.getXRot());
        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static boolean isNearPost(ServerPlayer player) {
        Post closest = Post.getClosest((int) player.getX(), (int) player.getZ());
        if (closest == null) {
            return false;
        }

        long dx = (long) player.getX() - closest.x();
        long dz = (long) player.getZ() - closest.z();
        int distance = (int) Math.sqrt(dx * dx + dz * dz);

        return distance <= Post.DIAMETER / 2;
    }
}
