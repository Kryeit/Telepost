package com.kryeit.telepost.compat;

import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.User;

import java.util.UUID;

public class GriefDefenderImpl {

    public static final int NEEDED_CLAIMBLOCKS = 80_000;
    public static int getClaimBlocks(UUID playerID) {
        User user = GriefDefender.getCore().getUser(playerID);
        return user == null ? -1 : user.getPlayerData().getInitialClaimBlocks() + user.getPlayerData().getAccruedClaimBlocks() + user.getPlayerData().getBonusClaimBlocks();
    }
}
