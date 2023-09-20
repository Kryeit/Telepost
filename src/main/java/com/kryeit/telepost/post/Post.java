package com.kryeit.telepost.post;

import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.storage.bytes.NamedPost;
import net.minecraft.util.math.Vec3d;

import java.util.Optional;

public class Post {

    public static final int DISTANCE_BETWEEN_POSTS = 50;
    public static final int WIDTH = 23;
    private final int x;
    private final int z;

    public Post(Vec3d pos) {
        this.x = (int) Math.round(pos.getX() / DISTANCE_BETWEEN_POSTS) * DISTANCE_BETWEEN_POSTS;
        this.z = (int) Math.round(pos.getZ() / DISTANCE_BETWEEN_POSTS) * DISTANCE_BETWEEN_POSTS;
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
        return 319;
    }

    public int getZ() {
        return z;
    }

    public Vec3d getLocation() {
        return new Vec3d(x + 0.5, getY(), z + 0.5);
    }

    public int[] getPostNumber() {
        int postX = x / DISTANCE_BETWEEN_POSTS;
        int postZ = z / DISTANCE_BETWEEN_POSTS;
        return new int[] { postX, postZ };
    }
}
