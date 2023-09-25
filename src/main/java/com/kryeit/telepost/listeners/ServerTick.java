package com.kryeit.telepost.listeners;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.post.GridIterator;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.post.PostBuilder;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;

import static com.kryeit.telepost.post.Post.WORLD;

public class ServerTick implements ServerTickEvents.EndTick {

    private int tickCounter = 0;

    private GridIterator gridIterator = null;

    @Override
    public void onEndTick(MinecraftServer server) {
        if (!Telepost.postBuilding) return;
        tickCounter++;
        if (gridIterator == null) gridIterator = new GridIterator();

        int timerInterval = 20;
        if (tickCounter >= timerInterval) {
            tickCounter = 0;

            if (gridIterator.hasNext()) {
                Vec3d loc = gridIterator.next();

                Post post = new Post(loc);
                PostBuilder.placeStructure(post);
                MinecraftServerSupplier.getServer().getPlayerManager().broadcast(Text.literal("Post " + post.getStringCoords() +
                        " is built in biome " + PostBuilder.getBiomeName(post.getBiome().getKey()) + " Height: " + post.getY() +
                        "And actual height: " + WORLD.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, post.getX(), post.getZ())), true);


            } else {
                Telepost.postBuilding = false;
            }
        }
    }
}