package com.kryeit.telepost.commands;

import com.kryeit.telepost.posts.Home;
import com.kryeit.telepost.posts.Post;
import com.kryeit.telepost.posts.PostBuilder;
import com.kryeit.telepost.posts.Relation;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.LivingEquipmentChangeEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class TeleportHandler {

    private static final Set<UUID> preventFalls = new HashSet<>();
    private static final Map<UUID, TeleportData> pendingTeleports = new HashMap<>();

    private static class TeleportData {
        Post destination;
        double startHeight;

        TeleportData(Post destination, double startHeight) {
            this.destination = destination;
            this.startHeight = startHeight;
        }
    }

    public static boolean visit(ServerPlayer player, String postName) {
        if (isInTeleport(player) || hasElytraEquipped(player)) {
            return false;
        }

        if (!isNearPost(player)) {
            player.sendSystemMessage(Component.literal("You need to be closer to a post"));
            return false;
        }

        Post post = Post.getByName(postName);
        if (post == null) {
            return false;
        }

        if (!canVisit(player, post)) {
            return false;
        }

        teleportToPost(player, post);
        return true;
    }

    public static boolean home(ServerPlayer player) {
        if (isInTeleport(player) || hasElytraEquipped(player)) {
            return false;
        }

        if (!isNearPost(player)) {
            player.sendSystemMessage(Component.literal("You need to be closer to a post"));
            return false;
        }

        Home home = Home.getByUser(player.getUUID());
        if (home == null) {
            return false;
        }

        Post post = Post.getById(home.postId());
        if (post == null) {
            return false;
        }

        teleportToPost(player, post);
        return true;
    }

    private static boolean isInTeleport(ServerPlayer player) {
        return pendingTeleports.containsKey(player.getUUID()) || preventFalls.contains(player.getUUID());
    }

    private static void teleportToPost(ServerPlayer player, Post post) {
        ServerLevel level = player.serverLevel();

        preventFalls.add(player.getUUID());
        pendingTeleports.put(player.getUUID(), new TeleportData(post, player.getY()));

        player.setDeltaMovement(new Vec3(0, 7, 0));
        player.hurtMarked = true;

        level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    private static boolean canVisit(ServerPlayer player, Post post) {
        if (post.owner() == null) {
            return true;
        }

        Relation.RelationType relation = Relation.getRelation(post.owner(), player.getUUID());
        return post.privated()
                ? relation == Relation.RelationType.ALLY
                : relation != Relation.RelationType.ENEMY;
    }

    private static boolean hasElytraEquipped(ServerPlayer player) {
        return player.getItemBySlot(net.minecraft.world.entity.EquipmentSlot.CHEST).getItem()
                instanceof net.minecraft.world.item.ElytraItem;
    }

    @SubscribeEvent
    public static void onEquipItem(LivingEquipmentChangeEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (event.getSlot() == net.minecraft.world.entity.EquipmentSlot.CHEST &&
                event.getTo().getItem() instanceof net.minecraft.world.item.ElytraItem) {

            if (isInTeleport(player)) {
                player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, event.getFrom());
                player.sendSystemMessage(net.minecraft.network.chat.Component.literal("Cannot equip elytra while teleporting"));
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        TeleportData data = pendingTeleports.get(player.getUUID());
        if (data != null) {
            double velocityY = player.getDeltaMovement().y;

            if (velocityY <= 0.1) {
                double heightDiff = player.getY() - data.startHeight;
                int destinationGroundY = PostBuilder.getSolidHeight(player.serverLevel(), data.destination.x(), data.destination.z());
                double destinationY = destinationGroundY + heightDiff;

                Vec3 velocity = player.getDeltaMovement();
                player.teleportTo(player.serverLevel(), data.destination.x() + 0.5, destinationY, data.destination.z() + 0.5, player.getYRot(), player.getXRot());
                player.setDeltaMovement(velocity);
                player.hurtMarked = true;


                pendingTeleports.remove(player.getUUID());
            }
        }
    }

    @SubscribeEvent
    public static void onFall(LivingFallEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }

        if (preventFalls.contains(player.getUUID())) {
            event.setCanceled(true);
            player.serverLevel().playSound(null, player.blockPosition(), SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
            preventFalls.remove(player.getUUID());
        }
    }

    private static boolean isNearPost(ServerPlayer player) {
        Post closest = Post.getClosest((int) player.getX(), (int) player.getZ());
        if (closest == null) {
            return false;
        }

        int dx = (int) player.getX() - closest.x();
        int dz = (int) player.getZ() - closest.z();
        int distance = (int) Math.sqrt(dx * dx + dz * dz);

        return distance <= Post.DIAMETER / 2;
    }
}