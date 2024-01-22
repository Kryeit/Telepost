package com.kryeit.telepost;

import com.kryeit.telepost.offlines.Offlines;
import com.kryeit.telepost.storage.bytes.NamedPost;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.UUID;

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

    public static boolean isPostNamedByAdmin(NamedPost post) {
        return !Telepost.getInstance().playerNamedPosts.getHashMap().containsKey(post.id());
    }

    public static String getNamedPostOwner(NamedPost post) {
        UUID id = Telepost.getInstance().playerNamedPosts.getElement(post.id());
        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(id);
        if (player != null)
            return player.getName().getString();

        return Offlines.getNameByUUID(id);
    }

    public static void executeCommandAsServer(String command) {
        // Create a command source that represents the server
        ServerCommandSource source = MinecraftServerSupplier.getServer().getCommandSource();

        // Execute the command
        MinecraftServerSupplier.getServer().getCommandManager().executeWithPrefix(source, command);
    }
}
