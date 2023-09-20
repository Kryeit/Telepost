package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.commands.completion.PlayerSuggestionProvider;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.function.Supplier;

public class Invite {
    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        Supplier<Text> message;

        ServerPlayerEntity invited = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        if (invited == null) {
            message = () -> Text.literal("Player not found");
            source.sendFeedback(message, false);
            return 0;
        }

        Optional<HomePost> home = Telepost.getDB().getHome(player.getUuid());

        if (home.isEmpty()) {
            message = () -> Text.literal("You don't have a home post, use /sethome").setStyle(Style.EMPTY.withFormatting(Formatting.RED));
            source.sendFeedback(message, false);
            return 0;
        }

        Telepost.invites.put(invited.getUuid(), player.getUuid());
        message = () -> Text.literal("You've invited " + name + " to your home post until the next restart").setStyle(Style.EMPTY.withFormatting(Formatting.GREEN));

        source.sendFeedback(message, false);

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
