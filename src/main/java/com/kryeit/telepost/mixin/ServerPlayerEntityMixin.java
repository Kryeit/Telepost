package com.kryeit.telepost.mixin;

import com.kryeit.telepost.utils.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class ServerPlayerEntityMixin {
    @Inject(method = "moveToWorld", at = @At("HEAD"))
    public void teleport(ServerWorld destination, CallbackInfoReturnable<Entity> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity instanceof ServerPlayerEntity player) {
            Utils.loadChunk(destination, player.getBlockX() >> 4, player.getBlockZ() >> 4);
        }
    }
}
