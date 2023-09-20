package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.Supplier;

public class NearestPost {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        Post post = new Post(player.getPos());

        Supplier<Text> message;

        if (post.isNamed()) {
            Optional<NamedPost> namedPost = post.getNamedPost();
            message = () -> Text.literal(
                    "The nearest post is at: ("
                            + post.getX() + ", "
                            + post.getZ() + "), it's " + namedPost.get().name());
        } else {
            message = () -> Text.literal(
                    "The nearest post is at: ("
                            + post.getX() + ", "
                            + post.getZ() + ")");
        }

        source.sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nearestpost")
                .executes(NearestPost::execute)
        );
    }
}
