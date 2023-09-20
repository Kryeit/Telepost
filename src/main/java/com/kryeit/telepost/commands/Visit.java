package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;
import java.util.function.Supplier;

public class Visit {
    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        Post post = new Post(player.getPos());

        if (!post.isInside(player.getPos())) {
            Supplier<Text> message = () -> Text.literal("You need to be standing on a post");
            source.sendFeedback(message, false);
            return 0;
        }

        Supplier<Text> message;

        ServerPlayerEntity visited = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        if (visited != null) {
            if (Telepost.invites.containsKey(visited.getUuid())) {
                message = () -> Text.literal("Welcome to " + name + " home post");
                
            }
        }

        Optional<NamedPost> namedPost = Telepost.getDB().getNamedPost(Utils.nameToId(name));
        if (namedPost.isEmpty()) {
            message = () -> Text.literal("The nearest post is not named!");
            source.sendFeedback(message, false);
            return 0;
        }

        Telepost.getDB().deleteNamedPost(Utils.nameToId(name));

        message = () -> Text.literal(
                "The nearest post has been unnamed at: ("
                        + post.getX() + ", "
                        + post.getZ() + ")");

        source.sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("visit")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                )
        );
    }
}
