package com.kryeit.telepost.commands;

import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.kryeit.telepost.worldedit.PostAccommodation;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.function.Supplier;

public class NearestPost {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player)) {
            Supplier<Text>  message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());

        Text text;

        post.build();

        Optional<NamedPost> namedPost = post.getNamedPost();
        if (namedPost.isPresent()) {
            text = TelepostMessages.getMessage(player, "telepost.nearest.named", Formatting.WHITE, post.getStringCoords(), namedPost.get().name());
        } else {
            text = TelepostMessages.getMessage(player, "telepost.nearest", Formatting.WHITE, post.getStringCoords());
        }
        player.sendMessage(text);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("nearestpost")
                .executes(NearestPost::execute)
        );

        dispatcher.register(CommandManager.literal("closestpost")
                .executes(NearestPost::execute)
        );

        dispatcher.register(CommandManager.literal("post")
                .executes(NearestPost::execute)
        );
    }
}
