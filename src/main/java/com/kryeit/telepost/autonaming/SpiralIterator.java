package com.kryeit.telepost.autonaming;

import com.kryeit.telepost.beans.Post;
import com.kryeit.telepost.beans.PostApi;
import com.kryeit.telepost.config.ConfigReader;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;
import java.util.Optional;

public class SpiralIterator implements Iterator<Post> {
    private int x = 0;
    private int y = 0;
    private int dx = 0;
    private int dy = -1;
    private final int gap;
    private int steps = 0;
    private final int maxSteps;

    public SpiralIterator() {
        int worldBorder = ConfigReader.PostSystem.INNER_WORLDBORDER;
        this.gap = ConfigReader.PostSystem.INNER_POST_CLEARANCE;
        this.maxSteps = (worldBorder / gap) * (worldBorder / gap);
    }

    @Override
    public boolean hasNext() {
        return steps < maxSteps;
    }

    @Override
    public Post next() {
        Vec3d pos = new Vec3d(x * gap, 0, y * gap);
        Optional<Post> current = PostApi.getClosest(pos);


        if ((x == y) || (x < 0 && x == -y) || (x > 0 && x == 1 - y)) {
            int temp = dx;
            dx = -dy;
            dy = temp;
        }

        x += dx;
        y += dy;
        steps++;

        return current.get();
    }
}

