package com.kryeit.telepost.entity;

import eu.pb4.polymer.core.api.entity.PolymerEntity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Arm;

import static com.kryeit.telepost.post.Post.WORLD;

public class DummyPlayerEntity extends LivingEntity implements PolymerEntity {
    private final PlayerEntity player;

    public DummyPlayerEntity(ServerPlayerEntity player) {
        super(EntityType.PLAYER, WORLD);
        this.player = player;
    }

    @Override
    public boolean shouldRenderName() {
        return true;
    }

    @Override
    public Iterable<ItemStack> getArmorItems() {
        return player.getArmorItems();
    }

    @Override
    public ItemStack getEquippedStack(EquipmentSlot slot) {
        return player.getEquippedStack(slot);
    }

    @Override
    public void equipStack(EquipmentSlot slot, ItemStack stack) {
    }

    @Override
    public Arm getMainArm() {
        return player.getMainArm();
    }

    @Override
    public EntityType<?> getPolymerEntityType(ServerPlayerEntity player) {
        return EntityType.PLAYER;
    }

    public void spawn() {
        setPos(player.getX(), player.getY(), player.getZ());
        setPitch(player.getPitch());
        setBodyYaw(player.bodyYaw);
        setHeadYaw(player.headYaw);
        WORLD.spawnEntity(this);
    }
}

