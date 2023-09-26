package com.kryeit.telepost.post;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.ClaimGroup;
import com.griefdefender.api.claim.ClaimResult;
import com.griefdefender.api.claim.ClaimTypes;
import com.griefdefender.api.data.ClaimData;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import com.griefdefender.lib.kyori.adventure.text.Component;
import com.kryeit.telepost.MinecraftServerSupplier;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.structure.StructureTemplateManager;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockBox;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.biome.Biome;

import java.util.Optional;

import static com.kryeit.telepost.post.Post.WIDTH;
import static com.kryeit.telepost.post.Post.WORLD;


public class PostBuilder {

    private static final Identifier DEFAULT_STRUCTURE = new Identifier("minecraft:default");
    public static final ClaimGroup claimGroup = ClaimGroup.builder().description(Component.text("Post claims")).name("Posts").build();


    public static String getBiomeName(Optional<RegistryKey<Biome>> biome) {
        if (biome.isEmpty()) return "";
        String input = biome.get().getValue().toString();
        String[] parts = input.split(":");

        return parts.length > 1 ? parts[1] : "";
    }

    public static Optional<StructureTemplate> getStructureTemplate(Optional<RegistryKey<Biome>> biome) {
        StructureTemplateManager manager = MinecraftServerSupplier.getServer().getStructureTemplateManager();
        Optional<StructureTemplate> template = manager.getTemplate(new Identifier(getBiomeName(biome)));

        if (template.isEmpty()) {
            template = manager.getTemplate(DEFAULT_STRUCTURE);
        }
        return template;
    }
    public static void placeStructure(Post post) {
        BlockPos pos = post.getBlockPos();
        RegistryEntry<Biome> biome = post.getBiome();

        Optional<StructureTemplate> template = getStructureTemplate(biome.getKey());

        if (template.isPresent()) {
            StructurePlacementData placementData = new StructurePlacementData()
                    .setMirror(BlockMirror.NONE)
                    .setRotation(BlockRotation.NONE)
                    .setIgnoreEntities(false);

            BlockBox boundingBox = template.get().calculateBoundingBox(placementData, pos);

            template.get().place(
                    ((ServerWorld) WORLD),
                    pos.add(-boundingBox.getMaxX()/2, -1, -boundingBox.getMaxZ()/2),
                    new BlockPos(0, 0, 0),
                    placementData,
                    WORLD.getRandom(),
                    2);
        }
    }

    public static void createClaim(Post post) {

        // Calculate the corners of the claim
        Vector3i lowerCorner = new Vector3i(post.getX() - WIDTH, post.getY() - 6, post.getZ() - WIDTH);
        Vector3i upperCorner = new Vector3i(post.getX() + WIDTH, WORLD.getHeight(), post.getZ() + WIDTH); // Set 255 as max Y value for the upper corner

        // Create the claim
        ClaimResult claimResult = Claim.builder()
                .bounds(lowerCorner, upperCorner)
                .world(GriefDefender.getCore().getWorldUniqueId(WORLD))
                .cuboid(true)
                .type(ClaimTypes.ADMIN)
                .build();
        if(claimResult.getClaim() == null) return;

        // Set the claim group
        ClaimData claimData = claimResult.getClaim().getData();
        claimData.setClaimGroupUniqueId(claimGroup.getUniqueId());
    }
}
