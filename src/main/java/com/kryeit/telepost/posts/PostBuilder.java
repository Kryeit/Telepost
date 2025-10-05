package com.kryeit.telepost.posts;

import com.kryeit.telepost.config.ConfigReader;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class PostBuilder {

    public static void copyStructures(Path worldPath) {
        Path structuresDir = worldPath.resolve("generated/minecraft/structures");

        try {
            Files.createDirectories(structuresDir);

            Path targetFile = structuresDir.resolve("default.nbt");

            if (!Files.exists(targetFile)) {
                InputStream stream = PostBuilder.class.getResourceAsStream("/structures/default.nbt");
                if (stream != null) {
                    Files.copy(stream, targetFile);
                    stream.close();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void place(ServerLevel level, String structureName, int x, int z) {
        StructureTemplateManager manager = level.getStructureManager();
        StructureTemplate template = manager.get(ResourceLocation.withDefaultNamespace(structureName)).orElse(null);

        int diameter = Post.DIAMETER;
        int radius = diameter / 2;
        int placementX = x - radius;
        int placementZ = z - radius;

        if (template == null) {
            return;
        }

        int y = getSolidHeight(level, x, z);

        int size = radius * 2 + 1;
        BlockPos from = new BlockPos(placementX, y, placementZ);
        BlockPos to = new BlockPos(placementX + size - 1, level.getMaxBuildHeight() - 1, placementZ + size - 1);

        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
        }

        BlockPos pos = new BlockPos(placementX, y, placementZ);

        StructurePlaceSettings settings = new StructurePlaceSettings();
        template.placeInWorld(level, pos, pos, settings, level.random, 2);
    }

    public static int getSolidHeight(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
    }
}