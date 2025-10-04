package com.kryeit.telepost;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.common.NeoForgeMod;

public class Utils {

    public static void broadcast(String message) {
        MinecraftServerSupplier.getServer().getPlayerList().broadcastSystemMessage(
                Component.literal(message), false
        );
    }

    public static boolean check(CommandSourceStack source, String permission, boolean fallback) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return source.hasPermission(2);
        }

        permission = "telepost." + permission;

        if (!ModList.get().isLoaded("luckperms")) {
            return fallback;
        }

        LuckPerms luckPerms = LuckPermsProvider.get();
        var user = luckPerms.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return fallback;
        }

        var result = user.getCachedData()
                .getPermissionData()
                .checkPermission(permission);

        return result.asBoolean() || (result == net.luckperms.api.util.Tristate.UNDEFINED && fallback);
    }

    public static int getMaxPosts(ServerPlayer player) {

        if (!ModList.get().isLoaded("luckperms")) {
            return 1;
        }

        LuckPerms luckPerms = LuckPermsProvider.get();

        var user = luckPerms.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return 1;
        }

        return user.getCachedData()
                .getPermissionData()
                .getPermissionMap()
                .keySet()
                .stream()
                .filter(perm -> perm.startsWith("telepost.posts."))
                .mapToInt(perm -> {
                    try {
                        return Integer.parseInt(perm.substring("telepost.posts.".length()));
                    } catch (NumberFormatException e) {
                        return 0;
                    }
                })
                .max()
                .orElse(1);
    }
}
