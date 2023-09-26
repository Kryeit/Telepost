package com.kryeit.telepost.post;

import com.griefdefender.api.GriefDefender;
import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostPermissions;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.kryeit.telepost.storage.bytes.NamedPost;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import org.apache.logging.log4j.core.jmx.Server;

import java.util.Optional;

public class Post {

    public static World WORLD = MinecraftServerSupplier.getServer().getOverworld();

    public static final int GAP = 50;
    public static final int WIDTH = 23;
    private final int x;
    private final int z;

    public Post(Vec3d pos) {
        this.x = (int) Math.round(pos.getX() / GAP) * GAP;
        this.z = (int) Math.round(pos.getZ() / GAP) * GAP;
    }

    public Post(HomePost home) {
        this.x = (int) home.location().getX();
        this.z = (int) home.location().getZ();
    }

    public Post(NamedPost namedPost) {
        this.x = (int) namedPost.location().getX();
        this.z = (int) namedPost.location().getZ();
    }

    public Post(int x, int z) {
        this.x = x;
        this.z = z;
    }

    public boolean isInside(ServerPlayerEntity player, Vec3d pos) {
        if (TelepostPermissions.isAdmin(player)) return true;
        int halfWidth = WIDTH / 2;

        boolean insideX = pos.getX() >= (x - halfWidth) && pos.getX() <= (x + halfWidth);
        boolean insideZ = pos.getZ() >= (z - halfWidth) && pos.getZ() <= (z + halfWidth);

        return insideX && insideZ;
    }

    public String getStringCoords() {
        return "(" + getX() + ", " + getZ() + ")";
    }

    public boolean isNamed() {
        return getNamedPost().isPresent();
    }

    public Optional<NamedPost> getNamedPost() {
        Optional<NamedPost> namedPost = Optional.empty();
        for (NamedPost named : Telepost.getInstance().database.getNamedPosts()) {
            int x = (int) named.location().getX();
            int z = (int) named.location().getZ();

            if (x == getX() && z == getZ()) {
                namedPost = Optional.of(named);
            }
        }
        return namedPost;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return WORLD.getTopY(Heightmap.Type.MOTION_BLOCKING_NO_LEAVES, x, z);
    }

    public int getZ() {
        return z;
    }

    public Vec3d getPos() {
        return new Vec3d(x + 0.5, getY(), z + 0.5);
    }

    public BlockPos getBlockPos() {
        return new BlockPos(x, getY(), z);
    }

    public RegistryEntry<Biome> getBiome() {
        return WORLD.getBiome(getBlockPos());
    }

    public void build() {
        PostBuilder.placeStructure(this);

        if (CompatAddon.GRIEF_DEFENDER.isLoaded()) {
            PostBuilder.createClaim(this);
        }
    }

    public void teleport(ServerPlayerEntity player) {
        player.teleport(getX() + 0.5, getY() + 1, getZ() + 0.5);
    }
}
