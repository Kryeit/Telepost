package com.kryeit.telepost.utils;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.beans.PostApi;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;


public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
    }
    public static boolean isInOverworld(ServerPlayerEntity player) {
        return player.getWorld().equals(PostApi.OVERWORLD);
    }
    public static boolean isInvited(ServerPlayerEntity owner, ServerPlayerEntity invited) {
        if (!Telepost.invites.containsKey(invited.getUuid())) return false;

        return Telepost.invites.get(invited.getUuid()).equals(owner.getUuid());
    }
    public static void loadChunk(World world, int chunkX, int chunkZ) {
        ChunkManager chunkManager = world.getChunkManager();
        chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    public static void executeCommandAsServer(String command) {
        // Create a command source that represents the server
        ServerCommandSource source = MinecraftServerSupplier.getServer().getCommandSource();

        // Execute the command
        MinecraftServerSupplier.getServer().getCommandManager().executeWithPrefix(source, command);
    }

    public static boolean check(ServerCommandSource source, String permission, boolean defaultValue) {
        User user = LuckPermsProvider.get().getUserManager().getUser(source.getPlayer().getUuid());

        if (user == null) {
            return defaultValue;
        }

        return user.getNodes(NodeType.PERMISSION).stream()
                .filter(NodeType.PERMISSION::matches)
                .map(NodeType.PERMISSION::cast)
                .anyMatch(node -> node.getPermission().equalsIgnoreCase(permission) && node.getValue());
    }
}
