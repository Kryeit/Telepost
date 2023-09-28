package com.kryeit.telepost;

import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.network.ServerPlayerEntity;

public class TelepostPermissions {

    public static boolean isAdmin(ServerPlayerEntity player) {
        return Permissions.check(player, "telepost.admin", false) || player.hasPermissionLevel(2);
    }

    public static boolean isHelper(ServerPlayerEntity player) {
        return Permissions.check(player, "telepost.helper", false);
    }

    public static boolean isHelperOrAdmin(ServerPlayerEntity player) {
        return isAdmin(player) || isHelper(player);
    }

    public static boolean isDefault(ServerPlayerEntity player) {
        return !isAdmin(player) && !isHelper(player);
    }
}
