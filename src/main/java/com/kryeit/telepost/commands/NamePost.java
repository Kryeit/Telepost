package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostPermissions;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.compat.GriefDefenderImpl;
import com.kryeit.telepost.post.Post;
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

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.kryeit.telepost.compat.GriefDefenderImpl.NEEDED_CLAIMBLOCKS;

public class NamePost {
    public static int execute(CommandContext<ServerCommandSource> context, String name) throws IOException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Supplier<Text> message;

        if (player == null || !Utils.isInOverworld(player)) {
            message = () -> Text.literal(I18n.translate("telepost.no_permission"));
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());

        // Check if nearest named
        if (post.isNamed()) {
            message = () -> Text.literal(I18n.translate("telepost.already_named")).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
            source.sendFeedback(message, false);
            return 0;
        }

        // Check if name is in use
        Optional<NamedPost> namedPost = Telepost.getDB().getNamedPost(Utils.nameToId(name));
        if (namedPost.isPresent()) {
            message = () -> Text.literal(I18n.translate("telepost.already_named")).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
            source.sendFeedback(message, false);
            return 0;
        }

        if (!TelepostPermissions.isAdmin(player) && CompatAddon.GRIEF_DEFENDER.isLoaded()) {
            if (GriefDefenderImpl.getClaimBlocks(player.getUuid()) < NEEDED_CLAIMBLOCKS) {
                message = () -> Text.literal(I18n.translate("telepost.name.claimblocks", NEEDED_CLAIMBLOCKS)).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
                source.sendFeedback(message, false);
                return 0;
            }
            Telepost.getInstance().playerNamedPosts.addElement(name, player.getUuid());
        }

        Telepost.getDB().addNamedPost(new NamedPost(Utils.nameToId(name), name, post.getPos()));

        message = () -> Text.literal(
                I18n.translate("telepost.named", name, post.getStringCoords())).setStyle(Style.EMPTY.withFormatting(Formatting.GREEN));

        source.sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("namepost")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            try {
                                return execute(context, StringArgumentType.getString(context, "name"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                )
        );
    }
}
