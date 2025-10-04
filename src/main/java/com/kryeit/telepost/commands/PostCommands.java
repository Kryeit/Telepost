package com.kryeit.telepost.commands;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.posts.PostBuilder;
import com.kryeit.telepost.storage.Database;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.posts.Home;
import com.kryeit.telepost.posts.Post;
import com.kryeit.telepost.posts.Relation;
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
                .executes(PostCommands::showClosestPost)
                .then(Commands.literal("list")
                        .requires(source -> Utils.check(source, "command.list", true))
                        .executes(PostCommands::postList))
                .then(Commands.literal("create")
                        .requires(source -> Utils.check(source, "command.create", true))
                        .then(Commands.argument("postName", StringArgumentType.string())
                                .then(Commands.argument("x", IntegerArgumentType.integer())
                                        .then(Commands.argument("z", IntegerArgumentType.integer())
                                                .executes(ctx -> createPost(ctx,
                                                        StringArgumentType.getString(ctx, "postName"),
                                                        IntegerArgumentType.getInteger(ctx, "x"),
                                                        IntegerArgumentType.getInteger(ctx, "z")))))))
                .then(Commands.literal("delete")
                        .requires(source -> Utils.check(source, "command.delete", false))
                        .then(Commands.argument("postName", StringArgumentType.string())
                                .executes(ctx -> deletePost(ctx, StringArgumentType.getString(ctx, "postName")))))
                .then(Commands.literal("rename")
                        .requires(source -> Utils.check(source, "command.rename", true))
                        .then(Commands.argument("oldName", StringArgumentType.string())
                                .then(Commands.argument("newName", StringArgumentType.string())
                                        .executes(ctx -> renamePost(ctx,
                                                StringArgumentType.getString(ctx, "oldName"),
                                                StringArgumentType.getString(ctx, "newName"))))))
                .then(Commands.literal("transfer")
                        .requires(source -> Utils.check(source, "command.transfer", true))
                        .then(Commands.argument("postName", StringArgumentType.string())
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(ctx -> transferPost(ctx,
                                                StringArgumentType.getString(ctx, "postName"),
                                                EntityArgument.getPlayer(ctx, "player"))))))
                .then(Commands.literal("privacy")
                        .requires(source -> Utils.check(source, "command.privacy", true))
                        .then(Commands.argument("postName", StringArgumentType.string())
                                .executes(ctx -> togglePrivacy(ctx, StringArgumentType.getString(ctx, "postName")))))
                .then(Commands.literal("ally")
                        .requires(source -> Utils.check(source, "command.ally", true))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> makeAlly(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("enemy")
                        .requires(source -> Utils.check(source, "command.enemy", true))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> makeEnemy(ctx, EntityArgument.getPlayer(ctx, "player")))))
                .then(Commands.literal("forgive")
                        .requires(source -> Utils.check(source, "command.forgive", true))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(ctx -> forgivePlayer(ctx, EntityArgument.getPlayer(ctx, "player"))))));
    }

    private static int transferPost(CommandContext<CommandSourceStack> ctx, String postName, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Post post = Post.getByName(postName);
        if (post == null || !post.owner().equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You don't own this post"));
            return 0;
        }

        Post.transfer(postName, player.getUUID(), target.getUUID());
        player.sendSystemMessage(Component.literal("Post transferred to " + target.getName().getString()));
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

    private static int visit(CommandContext<CommandSourceStack> ctx, String postName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (!TeleportHandler.visit(player, postName)) {
            player.sendSystemMessage(Component.literal("Cannot visit this post"));
            return 0;
        }

        return 1;
    }

    private static int home(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (!TeleportHandler.home(player)) {
            player.sendSystemMessage(Component.literal("No home set"));
            return 0;
        }

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

    private static int createPost(CommandContext<CommandSourceStack> ctx, String postName, int x, int z) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        if (player.hasPermissions(2)) {
            Database.getJdbi().useHandle(handle ->
                    handle.createUpdate("INSERT INTO posts (name, x, z) VALUES (:name, :x, :z)")
                            .bind("name", postName)
                            .bind("x", x)
                            .bind("z", z)
                            .execute()
            );

            PostBuilder.place(MinecraftServerSupplier.getServer().overworld(), "default", x, z);
            player.sendSystemMessage(Component.literal("Post created at " + x + ", " + z));
            return 1;
        }

        String error = Post.create(player.getUUID(), postName, x, z);
        if (error != null) {
            player.sendSystemMessage(Component.literal(error));
            return 0;
        }

        player.sendSystemMessage(Component.literal("Post created at " + x + ", " + z));
        return 1;
    }

    private static int deletePost(CommandContext<CommandSourceStack> ctx, String postName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();
        Post.delete(postName);
        player.sendSystemMessage(Component.literal("Post deleted"));
        return 1;
    }

    private static int renamePost(CommandContext<CommandSourceStack> ctx, String oldName, String newName) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Post post = Post.getByName(oldName);
        if (post == null || !post.owner().equals(player.getUUID())) {
            player.sendSystemMessage(Component.literal("You don't own this post"));
            return 0;
        }

        Post.rename(oldName, newName);
        player.sendSystemMessage(Component.literal("Post renamed to " + newName));
        return 1;
    }

    private static int makeAlly(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Relation.makeAlly(player.getUUID(), target.getUUID());
        player.sendSystemMessage(Component.literal(target.getName().getString() + " is now an ally"));
        return 1;
    }

    private static int makeEnemy(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Relation.makeEnemy(player.getUUID(), target.getUUID());
        player.sendSystemMessage(Component.literal(target.getName().getString() + " is now an enemy"));
        return 1;
    }

    private static int forgivePlayer(CommandContext<CommandSourceStack> ctx, ServerPlayer target) throws CommandSyntaxException {
        ServerPlayer player = ctx.getSource().getPlayerOrException();

        Relation.forgive(player.getUUID(), target.getUUID());
        player.sendSystemMessage(Component.literal(target.getName().getString() + " has been forgiven"));
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
        Component message = updatedPrivacy ? Component.literal("Post is now private, only allies are allowed") : Component.literal("Post is now public");
        player.sendSystemMessage(message);
        return 1;
    }

    private static boolean canVisit(ServerPlayer player, Post post) {
        Relation.RelationType relation = Relation.getRelation(post.owner(), player.getUUID());
        return post.privated()
                ? relation == Relation.RelationType.ALLY
                : relation != Relation.RelationType.ENEMY;
    }

    private static void teleportToPost(ServerPlayer player, Post post) {
        player.teleportTo(player.serverLevel(), post.x() + 0.5, PostBuilder.getSolidHeight(player.serverLevel(), post.x(), post.z()), post.z() + 0.5, player.getYRot(), player.getXRot());
    }

    private static int getDistance(ServerPlayer player, Post post) {
        int dx = (int) player.getX() - post.x();
        int dz = (int) player.getZ() - post.z();
        return (int) Math.sqrt(dx * dx + dz * dz);
    }
}