package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
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
                if (home.isEmpty()) {
                    message = () -> Text.literal( name + " doesn't have a home post");
                    source.sendFeedback(message, false);
                    return 0;
                }
                Post homePost = new Post(home.get());
                message = () -> Text.literal("Welcome to " + name + " home post");
                Utils.teleport(player, homePost);
                source.sendFeedback(message, false);
                return Command.SINGLE_SUCCESS;
            }
        }

        // /visit NamedPost
        String postName = Utils.getNameById(name);
        Optional<NamedPost> namedPostOptional = Telepost.getDB().getNamedPost(postName);
        if (namedPostOptional.isPresent()) {
            NamedPost named = namedPostOptional.get();
            Post namedPost = new Post(named);
            message = () -> Text.literal("Welcome to " + postName);
            source.sendFeedback(message, false);
            Utils.teleport(player, namedPost);
            return Command.SINGLE_SUCCESS;
        }

        return 0;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        List<String> suggestions = new ArrayList<>();
        for (NamedPost namedPost : Telepost.getDB().getNamedPosts()) {
            suggestions.add(namedPost.name());
        }
        suggestions.addAll(Arrays.asList(MinecraftServerSupplier.getServer().getPlayerManager().getPlayerNames()));

        SuggestionProvider<ServerCommandSource> suggestionProvider = (context, builder) ->
                CommandSource.suggestMatching(suggestions, builder);

        dispatcher.register(CommandManager.literal("visit")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .suggests(suggestionProvider)
                        .executes(context -> execute(context, StringArgumentType.getString(context, "name")))
                )
        );
    }
}
