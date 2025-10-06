package com.kryeit.telepost.gui;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class PostListMenuProvider implements MenuProvider {

    private final int initialPage;

    public PostListMenuProvider(int initialPage) {
        this.initialPage = initialPage;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Post List");
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory playerInventory, Player player) {
        return PostListMenu.sixRows(containerId, playerInventory,
                new PostListContainer((ServerPlayer) player, initialPage));
    }

    public static void open(ServerPlayer player, int page) {
        player.openMenu(new PostListMenuProvider(page));
    }
}