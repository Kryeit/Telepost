package com.kryeit.telepost.autonaming;

import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.post.Post;
import net.minecraft.util.math.Vec3d;

import java.util.Iterator;

public class SpiralIterator implements Iterator<Post> {
    private int x = 0;
    private int y = 0;
    private int dx = 0;
    private int dy = -1;
    private final int worldBorder;
    private final int gap;
    private int steps = 0;
    private final int maxSteps;

    public SpiralIterator() {
        this.worldBorder = ConfigReader.WORLDBORDER;
        this.gap = ConfigReader.GAP;
        this.maxSteps = (worldBorder / gap) * (worldBorder / gap);
    }

    @Override
    public boolean hasNext() {
        return steps < maxSteps;
    }

    @Override
    public Post next() {
        Vec3d pos = new Vec3d(x * gap, 0, y * gap);
        Post current = new Post(pos);

        if ((x == y) || (x < 0 && x == -y) || (x > 0 && x == 1 - y)) {
            int temp = dx;
            dx = -dy;
            dy = temp;
        }

        x += dx;
        y += dy;
        steps++;

        return current;
    }
}

