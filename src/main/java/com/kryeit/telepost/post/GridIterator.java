package com.kryeit.telepost.post;

import com.kryeit.telepost.beans.Post;
import com.kryeit.telepost.beans.PostApi;
import com.kryeit.telepost.config.ConfigReader;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Optional;

public class GridIterator implements Iterator<Vec3d> {
    private final int WORLDBORDER = ConfigReader.PostSystem.INNER_WORLDBORDER;
    private final int GAP = ConfigReader.PostSystem.INNER_POST_CLEARANCE;
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

        Optional<Post> post = PostApi.getClosest(new Vec3d(currentX, 0, currentZ));
        // Move to the next location in the grid.
        currentX += GAP;
        if (currentX > endX) {
            currentX = -(WORLDBORDER / GAP) * GAP;
            currentZ += GAP;
        }

        return post.get().getPos();
    }
}
