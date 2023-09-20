package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.commands.completion.PlayerSuggestionProvider;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.kryeit.telepost.storage.bytes.NamedPost;
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

public class Visit {
    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) return 0;

        Post closestPost = new Post(player.getPos());

        if (!closestPost.isInside(player.getPos())) {
            Supplier<Text> message = () -> Text.literal("You need to be standing on a post");
            source.sendFeedback(message, false);
            return 0;
        }

        Supplier<Text> message;

        ServerPlayerEntity visited = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        // /visit Player
        if (visited != null) {
            if (Telepost.invites.containsKey(player.getUuid()) && Telepost.invites.containsValue(visited.getUuid())) {
                Optional<HomePost> home = Telepost.getDB().getHome(visited.getUuid());
                Post homePost = new Post(home.get());
                message = () -> Text.literal("Welcome to " + name + " home post").setStyle(Style.EMPTY.withFormatting(Formatting.GREEN));
                homePost.teleport(player);
                source.sendFeedback(message, false);
                return Command.SINGLE_SUCCESS;
            } else {
                message = () -> Text.literal("You've not been invited").setStyle(Style.EMPTY.withFormatting(Formatting.RED));
                source.sendFeedback(message, false);
                return 0;
            }
        }

        // /visit NamedPost
        String postName = Utils.getNameById(name);
        Optional<NamedPost> namedPostOptional = Telepost.getDB().getNamedPost(postName);

        if (namedPostOptional.isPresent()) {
            Post namedPost = new Post(namedPostOptional.get());
            message = () -> Text.literal("Welcome to " + postName).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN));
            source.sendFeedback(message, false);
            namedPost.teleport(player);
            return Command.SINGLE_SUCCESS;
        }

        message = () -> Text.literal("Unknown post").setStyle(Style.EMPTY.withFormatting(Formatting.RED));
        source.sendFeedback(message, false);
        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("visit")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(PlayerSuggestionProvider.suggestPostNamesAndOnlinePlayers())
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                )
        );
    }
}
