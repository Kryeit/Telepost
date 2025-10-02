package com.kryeit.telepost;

import net.luckperms.api.LuckPerms;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.server.level.ServerPlayer;

public class Utils {

    public static boolean check(CommandSourceStack source, String permission, boolean fallback) {
        if (!(source.getEntity() instanceof ServerPlayer player)) {
            return source.hasPermission(2);
        }

        permission = "telepost." + permission;
        LuckPerms luckPerms = Telepost.luckPerms;

        if (luckPerms == null) {
            return fallback;
        }

        var user = luckPerms.getUserManager().getUser(player.getUUID());
        if (user == null) {
            return fallback;
        }

        return user.getCachedData()
                .getPermissionData()
                .checkPermission(permission)
                .asBoolean();
    }

    public static int getMaxPosts(ServerPlayer player) {
        LuckPerms luckPerms = Telepost.luckPerms;

        if (luckPerms == null) {
            return 1;
        }

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
