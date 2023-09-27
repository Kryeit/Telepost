package com.kryeit.telepost.commands;

import com.kryeit.telepost.*;
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
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Optional;
import java.util.function.Supplier;

public class Visit {
    public static int execute(CommandContext<ServerCommandSource> context, String name) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player))  {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post closestPost = new Post(player.getPos());

        Text text;

        if (!closestPost.isInside(player, player.getPos())) {
            text = TelepostMessages.getMessage("telepost.standing", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }
        
        ServerPlayerEntity visited = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        // /visit Player
        if (visited != null) {
            if (Telepost.invites.get(player.getUuid()).equals(visited.getUuid()) || TelepostPermissions.isHelperOrAdmin(player)) {
                Optional<HomePost> home = Telepost.getDB().getHome(visited.getUuid());
                if (home.isEmpty()) {
                    text = TelepostMessages.getMessage("telepost.no_homepost", Formatting.RED);
                    player.sendMessage(text);
                    return 0;
                }
                Post homePost = new Post(home.get());

                text = TelepostMessages.getMessage("telepost.teleport.homepost.other", Formatting.GREEN, name);
                player.sendMessage(text, true);

                homePost.teleport(player);
                return Command.SINGLE_SUCCESS;
            } else {
                text = TelepostMessages.getMessage("telepost.no_invite", Formatting.RED);
                player.sendMessage(text, true);
                return 0;
            }
        }

        // /visit NamedPost
        String postName = Utils.getNameById(name);
        Optional<NamedPost> namedPostOptional = Telepost.getDB().getNamedPost(postName);

        if (namedPostOptional.isPresent()) {
            Post namedPost = new Post(namedPostOptional.get());

            text = TelepostMessages.getMessage("telepost.teleport.named_post", Formatting.GREEN, postName);
            player.sendMessage(text, true);

            namedPost.teleport(player);
            return Command.SINGLE_SUCCESS;
        }

        text = TelepostMessages.getMessage("telepost.unknown_post", Formatting.RED);
        player.sendMessage(text, true);
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
