package com.kryeit.telepost.gui;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.posts.Post;
import com.kryeit.telepost.posts.Relation;
import com.mojang.authlib.GameProfile;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class PostListContainer extends SimpleContainer {
    private final ServerPlayer player;
    private final List<Post> posts;
    private int currentPage = 0;
    private static final int POSTS_PER_PAGE = 45;

    public PostListContainer(ServerPlayer player, int initialPage) {
        super(54);
        this.player = player;
        this.posts = Post.getVisible(player.getUUID());
        this.currentPage = Math.min(initialPage, (posts.size() - 1) / POSTS_PER_PAGE);
        populate();
    }
    
    public void populate() {
        for (int i = 0; i < getContainerSize(); i++) {
            setItem(i, ItemStack.EMPTY);
        }

        int startIndex = currentPage * POSTS_PER_PAGE;
        int endIndex = Math.min(startIndex + POSTS_PER_PAGE, posts.size());

        for (int i = startIndex; i < endIndex; i++) {
            Post post = posts.get(i);
            setItem(i - startIndex, createPostItem(post));
        }

        setItem(45, createPaneItem());
        setItem(46, createPaneItem());
        setItem(47, createPaneItem());

        if (currentPage > 0) {
            setItem(48, createPreviousPageItem());
        } else {
            setItem(48, createPaneItem());
        }

        setItem(49, createPageInfoItem());

        if (endIndex < posts.size()) {
            setItem(50, createNextPageItem());
        } else {
            setItem(50, createPaneItem());
        }

        setItem(51, createPaneItem());
        setItem(52, createPaneItem());
        setItem(53, createPaneItem());
    }

    private ItemStack createPostItem(Post post) {
        Relation.RelationType relation = Relation.getRelation(post.owner(), player.getUUID());

        ItemStack stack;
        if (post.owner() == null) {
            stack = Items.GREEN_TERRACOTTA.getDefaultInstance();
        } else if (post.owner().equals(player.getUUID())) {
            stack = Items.ORANGE_TERRACOTTA.getDefaultInstance();
        } else if (relation == Relation.RelationType.ALLY) {
            stack = Items.LIGHT_BLUE_TERRACOTTA.getDefaultInstance();
        } else {
            stack = Items.WHITE_TERRACOTTA.getDefaultInstance();
        }

        stack.set(DataComponents.ITEM_NAME, Component.literal(post.name() + " - (" + post.x() + ", " + post.z() + ")")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(false)));

        List<Component> lore = new ArrayList<>();

        String ownerName = post.owner() != null
                ? Objects.requireNonNull(MinecraftServerSupplier.getServer().getProfileCache())
                .get(post.owner())
                .map(GameProfile::getName)
                .orElse("Unknown")
                : null;

        if (ownerName != null) {
            lore.add(Component.literal(ownerName + "'s post")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY).withItalic(false)));
        }

        if (post.privated()) {
            lore.add(Component.literal("Private")
                    .withStyle(Style.EMPTY.withColor(ChatFormatting.RED).withItalic(false)));
        }

        lore.add(Component.literal("Click to teleport")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.AQUA).withItalic(false)));

        stack.set(DataComponents.LORE, new ItemLore(lore));
        return stack;
    }

    private ItemStack createPaneItem() {
        ItemStack stack = Items.GRAY_STAINED_GLASS_PANE.getDefaultInstance();
        stack.set(DataComponents.ITEM_NAME, Component.literal(" "));
        return stack;
    }

    private ItemStack createPreviousPageItem() {
        ItemStack stack = Items.ARROW.getDefaultInstance();
        stack.set(DataComponents.ITEM_NAME, Component.literal("Previous Page")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(false)));
        return stack;
    }

    private ItemStack createNextPageItem() {
        ItemStack stack = Items.ARROW.getDefaultInstance();
        stack.set(DataComponents.ITEM_NAME, Component.literal("Next Page")
                .withStyle(Style.EMPTY.withColor(ChatFormatting.YELLOW).withItalic(false)));
        return stack;
    }

    private ItemStack createPageInfoItem() {
        ItemStack stack = Items.PAPER.getDefaultInstance();
        int totalPages = (int) Math.ceil((double) posts.size() / POSTS_PER_PAGE);
        stack.set(DataComponents.ITEM_NAME, Component.literal("Page " + (currentPage + 1) + " / " + totalPages)
                .withStyle(Style.EMPTY.withColor(ChatFormatting.GOLD).withItalic(false)));
        return stack;
    }

    public void nextPage() {
        if ((currentPage + 1) * POSTS_PER_PAGE < posts.size()) {
            currentPage++;
            populate();
        }
    }

    public void previousPage() {
        if (currentPage > 0) {
            currentPage--;
            populate();
        }
    }

    public Post getPostAtSlot(int slot) {
        int index = currentPage * POSTS_PER_PAGE + slot;
        if (index >= 0 && index < posts.size()) {
            return posts.get(index);
        }
        return null;
    }
}