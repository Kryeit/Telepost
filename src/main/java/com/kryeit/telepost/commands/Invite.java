package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.commands.completion.PlayerSuggestionProvider;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;

public class Invite {
    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        ServerPlayerEntity invited = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        Text text;

        if (invited == null) {
            text = TelepostMessages.getMessage("telepost.unknown_player", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        Optional<HomePost> home = Telepost.getDB().getHome(player.getUuid());

        if (home.isEmpty()) {
            text = TelepostMessages.getMessage("telepost.no_homepost", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        Telepost.invites.put(invited.getUuid(), player.getUuid());

        text = TelepostMessages.getMessage("telepost.invite", Formatting.GREEN, name);
        player.sendMessage(text);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("invite")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(PlayerSuggestionProvider.suggestOnlinePlayers())
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                )
        );
    }
}
