package com.kryeit.telepost.compat;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;
import com.griefdefender.api.claim.*;
import com.griefdefender.api.data.ClaimData;
import com.griefdefender.lib.flowpowered.math.vector.Vector3i;
import com.griefdefender.lib.kyori.adventure.text.Component;
import com.kryeit.telepost.beans.Post;
import com.kryeit.telepost.beans.PostApi;
import com.kryeit.telepost.config.ConfigReader;

import java.util.UUID;


public class GriefDefenderImpl {

    public static int getClaimBlocks(UUID playerID) {
        User user = GriefDefender.getCore().getUser(playerID);
        return user == null ? -1 : user.getPlayerData().getInitialClaimBlocks() + user.getPlayerData().getAccruedClaimBlocks();
    }

    public static void createClaim(Post post) {
        final int WIDTH = ConfigReader.PostSystem.POST_WIDTH;
        ClaimGroup claimGroup = GriefDefender.getCore().getAdminClaimGroupsByName().get("posts");

        // Calculate the corners of the claim
        Vector3i lowerCorner = new Vector3i(post.x() - WIDTH, post.y(), post.z() - WIDTH);
        Vector3i upperCorner = new Vector3i(post.x() + WIDTH, PostApi.OVERWORLD.getHeight(), post.z() + WIDTH);

        // Create the claim
        ClaimResult claimResult = Claim.builder()
                .bounds(lowerCorner, upperCorner)
                .world(getWorldUUID())
                .cuboid(true)
                .denyMessages(true)
                .type(ClaimTypes.ADMIN)
                .build();
        if(claimResult.getClaim() == null) return;

        // Set the claim group
        ClaimData claimData = claimResult.getClaim().getData();
        claimData.setClaimGroupUniqueId(claimGroup.getUniqueId());
    }

    public static void createClaimGroup() {
        GriefDefender.getCore().deleteAdminClaimGroup("posts");
        ClaimGroup.builder()
                .description(Component.text("Post claims"))
                .name("posts")
                .type(ClaimGroupTypes.ADMIN)
                .syncMode(ClaimGroupSyncModes.ALL)
                .build();
    }

    public static Claim getClaim(Post post) {
        return GriefDefender.getCore().getClaimAt(getWorldUUID(), post.x(), PostApi.OVERWORLD.getHeight() - 10, post.z());
    }

    public static UUID getWorldUUID() {
        return GriefDefender.getCore().getWorldUniqueId(PostApi.OVERWORLD);
    }
}
