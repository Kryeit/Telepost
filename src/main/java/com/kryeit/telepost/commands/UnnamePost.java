package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostPermissions;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.post.Post;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static com.kryeit.telepost.Telepost.LOGGER;

public class UnnamePost {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Supplier<Text> message;

        if (player == null || !Utils.isInOverworld(player) || !TelepostPermissions.isAdmin(player)) {
            message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());
        String postName = StringArgumentType.getString(context, "name");
        String postID = Utils.nameToId(postName);

        Optional<NamedPost> namedPost = Telepost.getDB().getNamedPost(postID);
        if (namedPost.isEmpty()) {
            message = () -> Text.literal("The nearest post is not named");
            source.sendFeedback(message, false);
            return 0;
        }

        if (CompatAddon.BLUE_MAP.isLoaded()) {
            BlueMapImpl.removeMarker(postName);
        }

        try {
            Telepost.getInstance().playerNamedPosts.deleteElement(postID);
        } catch (IOException e) {
            e.printStackTrace();
            LOGGER.warn("Failed to delete player named post element (Was admin post?): " + postID);
        }

        Telepost.getDB().deleteNamedPost(postID);

        message = () -> Text.literal(
                "The nearest post has been unnamed at: " + post.getStringCoords());

        source.sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        List<String> suggestions = new ArrayList<>();

        for (NamedPost namedPost : Telepost.getDB().getNamedPosts()) {
            suggestions.add(namedPost.name());
        }

        SuggestionProvider<ServerCommandSource> suggestionProvider = (context, builder) ->
                CommandSource.suggestMatching(suggestions, builder);

        dispatcher.register(CommandManager.literal("unnamepost")
                .then(CommandManager.argument("name", StringArgumentType.greedyString())
                        .suggests(suggestionProvider)
                        .executes(UnnamePost::execute)
                )
        );
    }
}
