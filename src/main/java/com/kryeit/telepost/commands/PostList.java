package com.kryeit.telepost.commands;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.ClickEvent;
import net.minecraft.text.HoverEvent;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

public class PostList {
    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null) {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        List<NamedPost> posts = Telepost.getInstance().database.getNamedPosts();

        if (posts.isEmpty()) {
            player.sendMessage(TelepostMessages.getMessage(player, "telepost.postlist.no-posts", Formatting.RED), false);
            return 1;
        }
        Collections.sort(posts);

        int pages = (int) Math.ceil((double) posts.size() / 10);
        int page;
        try {
            page = IntegerArgumentType.getInteger(context, "page");
        } catch (IllegalArgumentException e) {
            page = 1;
        }

        if(page > pages || page < 1) {
            player.sendMessage(TelepostMessages.getMessage(player, "telepost.postlist.no-page", Formatting.RED), false);
            return 1;
        }

        // Header
        player.sendMessage(Text.literal(" ").formatted(Formatting.RESET), false);
        player.sendMessage(TelepostMessages.getMessage(player, "telepost.postlist.no-page", Formatting.GOLD), false);
        player.sendMessage(Text.literal("-----------------").formatted(Formatting.DARK_GRAY), false);

        int startIndex = (page - 1) * 10;
        int endIndex = Math.min(startIndex + 10, posts.size());

        for (int i = startIndex; i < endIndex; i++) {
            NamedPost post = posts.get(i);
            String name = post.name();
            MutableText postText = Text.literal((i + 1) + ". ").formatted(Formatting.GRAY)
                    .append(Text.literal(name).styled(style ->
                            style.withColor(Formatting.GRAY)
                                    .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/visit " + name))
                                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Visit " + name)))
                    ));

            player.sendMessage(postText, false);
        }

        // Footer - Pagination Arrows
        MutableText paginationText = Text.literal("Total posts: " + posts.size()).formatted(Formatting.LIGHT_PURPLE)
                .append(Text.literal(" || ").formatted(Formatting.GOLD));

        if (hasPreviousPage(page)) {
            int finalPage1 = page;
            paginationText.append(Text.literal("<<").styled(style ->
                    style.withColor(Formatting.GOLD)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/postlist " + (finalPage1 - 1)))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Previous Page")))
            ));
        } else {
            paginationText.append(Text.literal("<<").formatted(Formatting.GRAY));
        }

        paginationText.append(Text.literal(" -- ").formatted(Formatting.GREEN));

        if (hasNextPage(page, pages)) {
            int finalPage = page;
            paginationText.append(Text.literal(">>").styled(style ->
                    style.withColor(Formatting.GOLD)
                            .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/postlist " + (finalPage + 1)))
                            .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Text.literal("Next Page")))
            ));
        } else {
            paginationText.append(Text.literal(">>").formatted(Formatting.GRAY));
        }

        player.sendMessage(paginationText, false);


        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("postlist")
                .then(CommandManager.argument("page", IntegerArgumentType.integer(1))
                        .executes(PostList::execute))
        );
    }

    public static boolean hasPreviousPage(int currentPage) {
        return currentPage > 1;
    }

    public static boolean hasNextPage(int currentPage, int maxPages) {
        return currentPage < maxPages;
    }

}

