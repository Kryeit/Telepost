package com.kryeit.telepost.listeners;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.beans.Post;
import com.kryeit.telepost.beans.PostApi;
import com.kryeit.telepost.post.GridIterator;
import com.kryeit.telepost.post.PostBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class ServerTick implements ServerTickEvents.EndTick {

    private int tickCounter = 0;

    private GridIterator gridIterator = null;

    @Override
    public void onEndTick(MinecraftServer server) {
        if (!Telepost.postBuilding) return;
        tickCounter++;
        if (gridIterator == null) gridIterator = new GridIterator();

        int timerInterval = 1000;
        if (tickCounter >= timerInterval) {
            tickCounter = 0;

            if (gridIterator.hasNext()) {
                Vec3d loc = gridIterator.next();

                Optional<Post> post = PostApi.getClosest(loc);
                post.get().build();
                MinecraftServerSupplier.getServer().getPlayerManager().broadcast(Text.literal(
                        "Post " + post.get().getCoordinates() +
                        " is built in biome " + PostBuilder.getBiomeName(post.get().getBiome().getKey())),
                        true);


            } else {
                Telepost.postBuilding = false;
            }
        }
    }
}
