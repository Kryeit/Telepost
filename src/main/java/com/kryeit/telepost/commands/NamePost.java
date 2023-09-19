package com.kryeit.telepost.commands;

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

public class NamePost {
    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if(player == null) return 0;

        Post post = new Post(player.getPos());

        Supplier<Text> message;

        for (NamedPost named : Telepost.getDB().getNamedPosts()) {
            int x = (int) named.location().getX();
            int z = (int) named.location().getZ();

            if (x == post.getX() && z == post.getZ()) {
                message = () -> Text.literal("The nearest post is already named!");
                source.sendFeedback(message, false);
                return 0;
            }
        }

        Telepost.getDB().addNamedPost(new NamedPost(Utils.nameToId(name), name, post.getLocation()));

        message = () -> Text.literal(
                "The nearest post has been named " + name + " at: ("
                        + post.getX() + ", "
                        + post.getZ() + ")");

        source.sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("namepost")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                )
        );
    }
}
