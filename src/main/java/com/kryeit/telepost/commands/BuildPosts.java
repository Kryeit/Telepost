package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.compat.CompatAddon;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class BuildPosts {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player)) return 0;

        Supplier<Text> message;

        if (CompatAddon.GRIEF_DEFENDER.isLoaded()) {
            message = () -> Text.literal("Posts are starting to build and claim (GriefDefender is Loaded)");
        } else {
            message = () -> Text.literal("Posts are starting to build");
        }

        Telepost.postBuilding = true;

        source.sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("buildposts")
                .executes(BuildPosts::execute)
        );
    }
}
