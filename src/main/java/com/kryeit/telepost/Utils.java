package com.kryeit.telepost;

import com.kryeit.telepost.storage.bytes.NamedPost;
import net.minecraft.server.command.ServerCommandSource;

public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
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

    
     public static Structure loadStructure(Identifier structureId) {
    return WORLD.getStructureManager().getStructure(structureId);
    }
}
