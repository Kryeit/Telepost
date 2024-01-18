package com.kryeit.telepost;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;

import static com.kryeit.telepost.post.Post.WORLD;

public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
    }
    public static boolean isInOverworld(ServerPlayerEntity player) {
        return player.getWorld().equals(WORLD);
    }
    public static boolean isInvited(ServerPlayerEntity owner, ServerPlayerEntity invited) {
        return Telepost.invites.containsKey(invited.getUuid()) && Telepost.invites.get(invited.getUuid()).equals(owner.getUuid());
    }
    public static void loadChunk(int chunkX, int chunkZ) {
        ChunkManager chunkManager = WORLD.getChunkManager();
        chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

}
