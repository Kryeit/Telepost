package com.kryeit.telepost;

import com.kryeit.telepost.storage.bytes.NamedPost;
import net.minecraft.client.realms.dto.WorldDownload;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import static com.kryeit.telepost.post.Post.WORLD;

public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
    }

    public static boolean isInOverworld(ServerPlayerEntity player) {
        return player.getWorld().equals(WORLD);
    }

    public static String getNameById(String id) {
        for (NamedPost namedPost : Telepost.getDB().getNamedPosts()) {
            if (namedPost.id().equals(id.toLowerCase())) {
                return namedPost.name();
            }
        }
        return null;
    }

    public static void runCommand(String command, ServerCommandSource source) {
        MinecraftServerSupplier.getServer().getCommandManager().execute(MinecraftServerSupplier.getServer().getCommandManager().getDispatcher().parse(command, source), command);
    }

}
