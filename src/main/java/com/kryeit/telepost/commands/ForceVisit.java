package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.beans.NamedPost;
import com.kryeit.telepost.beans.Post;
import com.kryeit.telepost.beans.PostApi;
import com.kryeit.telepost.commands.completion.SuggestionsProvider;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.util.Optional;

public class ForceVisit {
    public static int execute(CommandContext<ServerCommandSource> context) {

        String postName = StringArgumentType.getString(context, "post");
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity player = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(playerName);

        if (player == null) return 0;

        Optional<NamedPost> namedPost = PostApi.NamedPostApi.get(postName);
        if (namedPost.isPresent()) {
            Post post = namedPost.get().asPost();
            post.teleport(player);
        }

        return 1;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("forcevisit")
                .requires(source -> Permissions.check(source, "telepost.forcevisit", false) || source.hasPermissionLevel(4))
                .then(CommandManager.argument("player", StringArgumentType.string())
                        .suggests(SuggestionsProvider.suggestOnlinePlayers())
                        .then(CommandManager.argument("post", StringArgumentType.greedyString())
                                .suggests(SuggestionsProvider.suggestPostNamesAndOnlinePlayers())
                                .executes(ForceVisit::execute)
                        )
                )
        );
    }
}
