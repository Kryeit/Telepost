package com.kryeit.telepost.beans;

import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.compat.GriefDefenderImpl;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.post.PostBuilder;
import com.kryeit.telepost.worldedit.PostAccommodation;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.biome.Biome;

import static com.kryeit.telepost.beans.PostApi.jdbi;

public record Post(long id, int x, int z) {

    public boolean isNamed() {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT COUNT(*) FROM named WHERE id = ?")
                        .bind(0, id)
                        .mapTo(Integer.class)
                        .findFirst()
                        .orElse(0) > 0
        );
    }

    public int y() {
        Heightmap.Type heightmapType = Heightmap.Type.MOTION_BLOCKING_NO_LEAVES;

        int x = this.x;
        int z = this.z;

        return PostApi.OVERWORLD.getTopY(heightmapType, x, z);
    }

    public String getCoordinates() {
        return "(" + x + ", " + y() + ", " + z + ")";
    }

    public void teleport(ServerPlayerEntity player) {
        player.teleport((ServerWorld) PostApi.OVERWORLD, x + 0.5, y() + 1, z + 0.5, player.getYaw(), player.getPitch());
    }

    public boolean isInside(ServerPlayerEntity player) {
        final int WIDTH = ConfigReader.PostSystem.POST_WIDTH;
        int halfWidth = (WIDTH - 1) / 2;

        boolean insideX = player.getBlockPos().getX() >= (x - halfWidth) && player.getBlockPos().getX() <= (x + halfWidth);
        boolean insideZ = player.getBlockPos().getZ() >= (z - halfWidth) && player.getBlockPos().getZ() <= (z + halfWidth);

        return insideX && insideZ;
    }

    public Vec3d getPos() {
        return new Vec3d(x + 0.5, y() + 1, z + 0.5);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, y(), z);
    }

    public RegistryEntry<Biome> getBiome() {
        return PostApi.OVERWORLD.getBiome(getBlockPos());
    }

    public void build() {
        if (CompatAddon.WORLD_EDIT.isLoaded()) {
            PostAccommodation.accommodate(this);
        }

        PostBuilder.placeStructure(this);

        if (CompatAddon.GRIEF_DEFENDER.isLoaded()) {
            GriefDefenderImpl.createClaim(this);
        }
    }
}
