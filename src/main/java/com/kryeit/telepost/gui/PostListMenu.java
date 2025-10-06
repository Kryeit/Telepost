package com.kryeit.telepost.gui;

import com.kryeit.telepost.commands.TeleportHandler;
import com.kryeit.telepost.posts.Post;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class PostListMenu extends AbstractContainerMenu {
    private final Container container;
    private final int containerRows;

    public static PostListMenu sixRows(int containerId, Inventory playerInventory, Container container) {
        return new PostListMenu(MenuType.GENERIC_9x6, containerId, playerInventory, container, 6);
    }

    public PostListMenu(MenuType<?> type, int containerId, Inventory playerInventory, Container container, int rows) {
        super(type, containerId);
        checkContainerSize(container, rows * 9);
        this.container = container;
        this.containerRows = rows;
        container.startOpen(playerInventory.player);

        for(int j = 0; j < this.containerRows; ++j) {
            for(int k = 0; k < 9; ++k) {
                this.addSlot(new PostSlot(container, k + j * 9, 8 + k * 18, 18 + j * 18));
            }
        }

        int yOffset = (this.containerRows - 4) * 18;
        for(int l = 0; l < 3; ++l) {
            for(int j1 = 0; j1 < 9; ++j1) {
                this.addSlot(new Slot(playerInventory, j1 + l * 9 + 9, 8 + j1 * 18, 103 + l * 18 + yOffset));
            }
        }

        for(int i1 = 0; i1 < 9; ++i1) {
            this.addSlot(new Slot(playerInventory, i1, 8 + i1 * 18, 161 + yOffset));
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.container.stillValid(player);
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.container.stopOpen(player);
    }

    @Override
    public void clicked(int slotId, int dragType, ClickType clickType, Player p) {
        if (slotId < 0 || slotId >= this.containerRows * 9) {
            super.clicked(slotId, dragType, clickType, p);
            return;
        }

        ServerPlayer player = (ServerPlayer) p;
        PostListContainer container = (PostListContainer) this.container;

        if (clickType == ClickType.PICKUP && dragType == 0) {
            if (slotId == 48) {
                container.previousPage();
                return;
            } else if (slotId == 50) {
                container.nextPage();
                return;
            } else if (slotId < 45) {
                Post post = container.getPostAtSlot(slotId);
                if (post != null) {
                    TeleportHandler.visit(player, post.name());
                    player.closeContainer();
                }
                return;
            }
        }

        super.clicked(slotId, dragType, clickType, p);
    }

    private static class PostSlot extends Slot {
        public PostSlot(Container container, int index, int x, int y) {
            super(container, index, x, y);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public boolean mayPickup(Player player) {
            return false;
        }
    }
}