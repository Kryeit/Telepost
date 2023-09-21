package com.kryeit.telepost.post;

import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.kryeit.telepost.post.Post.GAP;
import static com.kryeit.telepost.post.Post.WORLD;

public class GridIterator implements Iterator<Vec3d> {
    public int borderRadius = WORLD.getWorldBorder().getMaxRadius();
    private final int endX;
    private final int endZ;
    private int currentX = 0;
    private int currentZ = 0;

    public GridIterator() {
        this.endX = (borderRadius / GAP) * GAP;
        this.endZ = (borderRadius / GAP) * GAP;
    }

    @Override
    public boolean hasNext() {
        return currentZ <= endZ;
    }

    @Override
    public Vec3d next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }

        Post post = new Post(currentX, currentZ);
        // Move to the next location in the grid.
        currentX += GAP;
        if (currentX > endX) {
            currentX = 0;
            currentZ += GAP;
        }

        return post.getPos();
    }
}
