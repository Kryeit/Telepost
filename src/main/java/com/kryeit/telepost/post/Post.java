package com.kryeit.telepost.post;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.storage.bytes.HomePost;
import com.kryeit.telepost.storage.bytes.NamedPost;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;

import java.util.Optional;

public class Post {

    public static final World WORLD = MinecraftServerSupplier.getServer().getWorld(World.OVERWORLD);
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

    public boolean isInside(Vec3d pos) {
        int halfWidth = WIDTH / 2;

        boolean insideX = pos.getX() >= (x - halfWidth) && pos.getX() <= (x + halfWidth);
        boolean insideZ = pos.getZ() >= (z - halfWidth) && pos.getZ() <= (z + halfWidth);

        return insideX && insideZ;
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
        return WORLD.getTopY(Heightmap.Type.MOTION_BLOCKING, x, z) + 2;
    }

    public int getZ() {
        return z;
    }

    public Vec3d getPos() {
        return new Vec3d(x + 0.5, getY(), z + 0.5);
    }

    public void teleport(ServerPlayerEntity player) {
        player.teleport(getX(), getY(), getZ());

    }

    public int[] getPostNumber() {
        int postX = x / GAP;
        int postZ = z / GAP;
        return new int[] { postX, postZ };
    }
}
