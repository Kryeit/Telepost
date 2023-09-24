package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
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

        if (player == null || !Utils.isInOverworld(player)) return 0;

        Post post = new Post(player.getPos());

        if (!post.isInside(player.getPos())) {
            Supplier<Text> message = () -> Text.literal("You need to be standing on a post").setStyle(Style.EMPTY.withFormatting(Formatting.RED));
            source.sendFeedback(message, false);
            return 0;
        }

        Optional<HomePost> home = Telepost.getDB().getHome(player.getUuid());

        Supplier<Text> message;
        if (home.isPresent()) {
            message = () -> Text.literal("You've been teleported to your home post").setStyle(Style.EMPTY.withFormatting(Formatting.GREEN));
            Post homePost = new Post(home.get());
            homePost.teleport(player);
        } else message = () -> Text.literal("You don't have a home post, make one with /sethome").setStyle(Style.EMPTY.withFormatting(Formatting.RED));

        source.sendFeedback(message, false);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("home")
                .executes(Home::execute)
        );
    }
}
