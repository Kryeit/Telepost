package com.kryeit.telepost.commands;

import com.kryeit.telepost.Utils;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.function.Supplier;

public class NearestPost {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Supplier<Text> message;

        if (player == null || !Utils.isInOverworld(player)) {
            message = () -> Text.literal("You can't execute the command");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());

        Optional<NamedPost> namedPost = post.getNamedPost();
        message = namedPost.<Supplier<Text>>map(
                value -> () -> Text.literal(
                "The nearest post is at: " + post.getStringCoords() + ", it's " + value.name()).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN)))
                .orElseGet(() -> () -> Text.literal(
                "The nearest post is at: " + post.getStringCoords()).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN)));

        source.sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nearestpost")
                .executes(NearestPost::execute)
        );
    }
}
