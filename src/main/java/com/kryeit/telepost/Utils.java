package com.kryeit.telepost;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.fml.ModList;

import java.util.UUID;

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

    public static int getMaxPosts(UUID uuid) {

        if (!ModList.get().isLoaded("luckperms")) {
            return 1;
        }

        LuckPerms luckPerms = LuckPermsProvider.get();

        var user = luckPerms.getUserManager().getUser(uuid);
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

    public static boolean hasBlockAbove(ServerPlayer player) {
        Level level = player.level();
        BlockPos pos = player.blockPosition();
        int checkY = pos.getY() + 2;

        double minX = player.getX() - 0.3;
        double maxX = player.getX() + 0.3;
        double minZ = player.getZ() - 0.3;
        double maxZ = player.getZ() + 0.3;

        for (int x = Mth.floor(minX); x <= Mth.floor(maxX); x++) {
            for (int z = Mth.floor(minZ); z <= Mth.floor(maxZ); z++) {
                BlockState state = level.getBlockState(new BlockPos(x, checkY, z));
                if (!state.isAir() && state.isSolidRender(level, new BlockPos(x, checkY, z))) {
                    return true;
                }
            }
        }
        return false;
    }
}
