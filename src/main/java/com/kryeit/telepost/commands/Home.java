package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
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

public class Home {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        Supplier<Text> message;

        if (player == null || !Utils.isInOverworld(player)) {
            message = () -> Text.literal(I18n.translate("telepost.no_permission"));
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());

        if (!post.isInside(player, player.getPos())) {
            message = () -> Text.literal(I18n.translate("telepost.standing")).setStyle(Style.EMPTY.withFormatting(Formatting.RED));
            source.sendFeedback(message, false);
            return 0;
        }

        Optional<HomePost> home = Telepost.getDB().getHome(player.getUuid());

        if (home.isPresent()) {
            player.sendMessage(
                    Text.literal(I18n.translate("telepost.teleport.homepost"))
                            .setStyle(Style.EMPTY.withFormatting(Formatting.GREEN))
                    , true);

            Post homePost = new Post(home.get());
            homePost.teleport(player);
        } else player.sendMessage(
                Text.literal(I18n.translate("telepost.no_homepost"))
                        .setStyle(Style.EMPTY.withFormatting(Formatting.RED)));

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("home")
                .executes(Home::execute)
        );
    }
}
