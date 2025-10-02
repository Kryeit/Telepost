package com.kryeit.telepost.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.posts.Home;
import com.kryeit.telepost.posts.Post;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class PostCommands {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("visit")
                .requires(source -> Utils.check(source, "command.visit", true))
                .then(Commands.argument("postName", StringArgumentType.string())
                        .executes(ctx -> visit(ctx, StringArgumentType.getString(ctx, "postName")))));

        dispatcher.register(Commands.literal("forcevisit")
                .requires(source -> Utils.check(source, "command.forcevisit", false))
                .then(Commands.argument("postName", StringArgumentType.string())
                        .executes(ctx -> forceVisit(ctx, StringArgumentType.getString(ctx, "postName")))));

        dispatcher.register(Commands.literal("home")
                .requires(source -> Utils.check(source, "command.home", true))
                .executes(PostCommands::home));

        dispatcher.register(Commands.literal("sethome")
                .requires(source -> Utils.check(source, "command.sethome", true))
                .executes(PostCommands::setHome));

        dispatcher.register(Commands.literal("post")
                .requires(source -> Utils.check(source, "command.post", true))
                .executes(PostCommands::showClosestPost));

        dispatcher.register(Commands.literal("postlist")
                .requires(source -> Utils.check(source, "command.postlist", true))
                .executes(PostCommands::postList));

        dispatcher.register(Commands.literal("postprivacy")
                .requires(source -> Utils.check(source, "command.postprivacy", true))
                .then(Commands.argument("postName", StringArgumentType.string())
                        .executes(ctx -> togglePrivacy(ctx, StringArgumentType.getString(ctx, "postName")))));

        dispatcher.register(Commands.literal("posttrust")
                .requires(source -> Utils.check(source, "command.posttrust", true))
                .then(Commands.argument("postName", StringArgumentType.string())
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> trustPlayer(ctx, StringArgumentType.getString(ctx, "postName"), EntityArgument.getPlayer(ctx, "player"))))));

        dispatcher.register(Commands.literal("deletepost")
                .requires(source -> Utils.check(source, "command.deletepost", false))
                .then(Commands.argument("postName", StringArgumentType.string())
                        .executes(ctx -> deletePost(ctx, StringArgumentType.getString(ctx, "postName")))));
    }

    private static int visit(CommandContext<CommandSourceStack> ctx, String postName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Post post = Post.getByName(postName);
        if (post == null) {
            player.sendSystemMessage(Component.literal("Post not found"));
            return 0;
        }

        if (!canVisit(player, post)) {
            player.sendSystemMessage(Component.literal("You cannot visit this private post"));
            return 0;
        }

        teleportToPost(player, post);
        return 1;
    }

    private static int forceVisit(CommandContext<CommandSourceStack> ctx, String postName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Post post = Post.getByName(postName);
        if (post == null) {
            player.sendSystemMessage(Component.literal("Post not found"));
            return 0;
        }

        teleportToPost(player, post);
        return 1;
    }

    private static int home(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Home home = Home.getByUser(player.getUUID());
        if (home == null) {
            player.sendSystemMessage(Component.literal("You don't have a home set"));
            return 0;
        }

        Post post = Post.getById(home.postId());
        if (post == null) {
            player.sendSystemMessage(Component.literal("Home post not found"));
            return 0;
        }

        teleportToPost(player, post);
        return 1;
    }

    private static int setHome(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Post closest = Post.getClosest((int) player.getX(), (int) player.getZ());
        if (closest == null || getDistance(player, closest) > Post.GAP) {
            player.sendSystemMessage(Component.literal("No post nearby"));
            return 0;
        }

        Home.setHome(player.getUUID(), closest.id());
        player.sendSystemMessage(Component.literal("Home set"));
        return 1;
    }

    private static int showClosestPost(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Post closest = Post.getClosest((int) player.getX(), (int) player.getZ());
        if (closest == null) {
            player.sendSystemMessage(Component.literal("No posts found"));
            return 0;
        }

        int distance = getDistance(player, closest);
        player.sendSystemMessage(Component.literal("Closest post: " + closest.name() + " (" + distance + " blocks away)"));
        return 1;
    }

    private static int postList(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        player.sendSystemMessage(Component.literal("Post list GUI coming soon"));
        return 1;
    }

    private static int togglePrivacy(CommandContext<CommandSourceStack> ctx, String postName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Post post = Post.getByName(postName);
        if (post == null || !post.owner().equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You don't own this post"));
            return 0;
        }

        boolean updatedPrivacy = Post.togglePrivacy(postName);
        Component message = updatedPrivacy ? Component.literal("Post is now private, use /posttrust to allow a player to visit it") : Component.literal("Post is now public");
        player.sendSystemMessage(message);
        return 1;
    }

    private static int trustPlayer(CommandContext<CommandSourceStack> ctx, String postName, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Post post = Post.getByName(postName);
        if (post == null || !post.owner().equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You don't own this post"));
            return 0;
        }

        Post.addTrust(postName, target.getUUID());
        player.sendSystemMessage(Component.literal("Player trusted"));
        return 1;
    }

    private static int deletePost(CommandContext<CommandSourceStack> ctx, String postName) {
        Post.delete(postName);
        ctx.getSource().sendSystemMessage(Component.literal("Post deleted"));
        return 1;
    }

    private static boolean canVisit(ServerPlayer player, Post post) {
        return !post.privated() || post.owner().equals(player.getUUID()) || post.allowed().contains(player.getUUID());
    }

    private static void teleportToPost(ServerPlayer player, Post post) {
        player.teleportTo(player.serverLevel(), post.x(), player.getY(), post.z(), player.getYRot(), player.getXRot());
    }

    private static int getDistance(ServerPlayer player, Post post) {
        int dx = (int) player.getX() - post.x();
        int dz = (int) player.getZ() - post.z();
        return (int) Math.sqrt(dx * dx + dz * dz);
    }
}