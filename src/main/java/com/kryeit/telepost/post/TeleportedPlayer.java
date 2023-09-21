package com.kryeit.telepost.post;

import com.kryeit.telepost.Utils;
import com.kryeit.telepost.entity.DummyPlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.kryeit.telepost.post.Post.WORLD;

public class TeleportedPlayer {

    private ServerPlayerEntity player;
    private Post post;

    public TeleportedPlayer(ServerPlayerEntity player, Post post) {
        this.player = player;
        this.post = post;
    }

    public void createFakePlayer() {
        DummyPlayerEntity dummy = new DummyPlayerEntity(player);
        dummy.spawn();
    }

    public void handle() {
        createFakePlayer();
    }

    public void setSpectator() {
        Utils.runCommand("gamemode spectator " + player.getName(), player.getCommandSource());
    }

    public void setSurvival() {
        Utils.runCommand("gamemode survival " + player.getName(), player.getCommandSource());
    }

    public void exitBody() {

    }

    public void enterBody() {

    }

    public void goingUp() {

    }

    public void goingDown() {

    }

    public void movingCameraToPost() {

    }

    public void teleport() {
        player.teleport(post.getX(), post.getY(), post.getZ());
    }
}
