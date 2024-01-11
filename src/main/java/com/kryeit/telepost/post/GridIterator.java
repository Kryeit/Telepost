package com.kryeit.telepost.post;

import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.NoSuchElementException;

import static com.kryeit.telepost.config.ConfigReader.GAP;
import static com.kryeit.telepost.config.ConfigReader.WORLDBORDER;

public class GridIterator implements Iterator<Vec3d> {
    private final int endX;
    private final int endZ;
    private int currentX = -(WORLDBORDER / GAP) * GAP;;
    private int currentZ = -(WORLDBORDER / GAP) * GAP;;

    public GridIterator() {
        this.endX = (WORLDBORDER / GAP) * GAP;
        this.endZ = (WORLDBORDER / GAP) * GAP;
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
            currentX = -(WORLDBORDER / GAP) * GAP;
            currentZ += GAP;
        }

        return post.getPos();
    }
}
