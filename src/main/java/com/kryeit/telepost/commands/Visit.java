package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostPermissions;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.commands.completion.PlayerSuggestionProvider;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.client.resource.language.I18n;
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

        Supplier<Text> message;

        if (player == null || !Utils.isInOverworld(player))  {
            message = () -> Text.literal(I18n.translate("telepost.no_permission")).setStyle(Style.EMPTY.withFormatting(Formatting.RED));;
            source.sendFeedback(message, false);
            return 0;
        }

        Post closestPost = new Post(player.getPos());

        if (!closestPost.isInside(player, player.getPos())) {
            message = () -> Text.literal(I18n.translate("telepost.standing")).setStyle(Style.EMPTY.withFormatting(Formatting.RED));;
            source.sendFeedback(message, false);
            return 0;
        }
        
        ServerPlayerEntity visited = MinecraftServerSupplier.getServer().getPlayerManager().getPlayer(name);

        // /visit Player
        if (visited != null) {
            if (Telepost.invites.get(player.getUuid()).equals(visited.getUuid()) || TelepostPermissions.isHelperOrAdmin(player)) {
                Optional<HomePost> home = Telepost.getDB().getHome(visited.getUuid());
                if (home.isEmpty()) {
                    message = () -> Text.literal(I18n.translate("telepost.no_homepost")).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
                    source.sendFeedback(message, false);
                    return 0;
                }
                Post homePost = new Post(home.get());
                player.sendMessage(
                        Text.literal(I18n.translate("telepost.teleport.homepost.other", name)).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN)),
                        true);

                homePost.teleport(player);
                return Command.SINGLE_SUCCESS;
            } else {
                message = () -> Text.literal(I18n.translate("telepost.no_invite")).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
                source.sendFeedback(message, false);
                return 0;
            }
        }

        // /visit NamedPost
        String postName = Utils.getNameById(name);
        Optional<NamedPost> namedPostOptional = Telepost.getDB().getNamedPost(postName);

        if (namedPostOptional.isPresent()) {
            Post namedPost = new Post(namedPostOptional.get());
            player.sendMessage(
                    Text.literal(I18n.translate("telepost.teleport.named_post", postName)).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN)),
                    true);

            namedPost.teleport(player);
            return Command.SINGLE_SUCCESS;
        }

        message = () -> Text.literal(I18n.translate("telepost.unknown_post")).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
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
