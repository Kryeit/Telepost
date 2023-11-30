package com.kryeit.telepost.worldedit;

import com.kryeit.telepost.post.Post;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.MaxChangedBlocksException;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.fabric.FabricAdapter;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.math.Vector3;
import com.sk89q.worldedit.math.convolution.GaussianKernel;
import com.sk89q.worldedit.math.convolution.HeightMap;
import com.sk89q.worldedit.math.convolution.HeightMapFilter;
import com.sk89q.worldedit.regions.CuboidRegion;
import com.sk89q.worldedit.regions.Region;
import com.sk89q.worldedit.world.block.BaseBlock;
import com.sk89q.worldedit.world.block.BlockCategories;
import com.sk89q.worldedit.world.block.BlockType;
import com.sk89q.worldedit.world.block.BlockTypes;
import net.minecraft.block.Blocks;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.kryeit.telepost.post.Post.WIDTH;
import static com.kryeit.telepost.post.Post.WORLD;

public class PostAccommodation {
    public static void accommodate(Post post) {
        int width = ((WIDTH - 1) / 2) + 5;
        int biggerWidth = ((WIDTH - 1) / 2) + 7;

        int x = post.getX();
        int y = post.getY();
        int z = post.getZ();

        EditSession editSession = getEditSession();

        Vector3 start;
        Vector3 end;

        start = Vector3.at(x + width, y, z + width);
        end = Vector3.at(x - width, y + 100, z - width);

        cut(editSession, start, end);

        start = Vector3.at(x + width, y + 20, z + width);
        end = Vector3.at(x - width, y - 10, z - width);

        try {
            editSession.replaceBlocks(
                    new CuboidRegion(editSession.getWorld(), start.toBlockPoint(), end.toBlockPoint()),
                    getFoliage(),
                    BlockTypes.AIR.getDefaultState()
            );

            editSession.makeCylinder(BlockVector3.at(x, post.getY() - 15, z), editSession.getBlock(BlockVector3.at(x, post.getY() - 1, z)), width, width, 15, true);
            editSession.close();
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }

        start = Vector3.at(x + biggerWidth, y - 10, z + biggerWidth);
        end = Vector3.at(x - biggerWidth, y + 100, z - biggerWidth);

        smooth(start, end);
    }

    private static void cut(EditSession editSession, Vector3 start, Vector3 end) {
        Region region = new CuboidRegion(editSession.getWorld(), start.toBlockPoint(), end.toBlockPoint());
        try {
            editSession.setBlocks(region, FabricAdapter.adapt(Blocks.AIR).getDefaultState());
        } catch (MaxChangedBlocksException e) {
            e.printStackTrace();
        }
    }

    public static void smooth(Vector3 start, Vector3 end) {

        EditSession editSession = getEditSession();

        Region region = new CuboidRegion(editSession.getWorld(), start.toBlockPoint(), end.toBlockPoint());

        HeightMap heightMap = new HeightMap(editSession, region, null);
        HeightMapFilter filter = new HeightMapFilter(new GaussianKernel(5, 1.0));
        try {
            heightMap.applyFilter(filter, 3);
        } catch (MaxChangedBlocksException e) {
            throw new RuntimeException(e);
        }
        editSession.close();
    }

    public static Set<BaseBlock> getFoliage() {
        List<BlockType> logs = new ArrayList<>(BlockCategories.LOGS.getAll().stream().toList());
        List<BlockType> leaves = new ArrayList<>(BlockCategories.LEAVES.getAll().stream().toList());

        Set<BaseBlock> baseBlocks = new HashSet<>();

        for (BlockType block : logs) {
            baseBlocks.add(block.getDefaultState().toBaseBlock());
        }

        for (BlockType block : leaves) {
            baseBlocks.add(block.getDefaultState().toBaseBlock());
        }

        return baseBlocks;
    }

    public static EditSession getEditSession() {
        return WorldEdit.getInstance().newEditSession(FabricAdapter.adapt(WORLD));
    }
}
