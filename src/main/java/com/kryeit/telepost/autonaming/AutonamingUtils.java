package com.kryeit.telepost.autonaming;

import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.beans.Post;
import com.kryeit.telepost.beans.PostApi;
import com.kryeit.telepost.utils.Utils;
import net.minecraft.util.Formatting;

import java.util.ArrayList;
import java.util.List;

import static com.kryeit.telepost.Telepost.LOGGER;
import static com.kryeit.telepost.config.ConfigReader.POST_NAMES;

public class AutonamingUtils {

    public static String getRandom() {
        List<String> availableNames = new ArrayList<>();

        for (String name : POST_NAMES) {
            if (PostApi.NamedPostApi.get(name).isPresent())
                continue;

            availableNames.add(name);
        }

        if (availableNames.isEmpty()) {
            LOGGER.warn("Config file for Telepost doesn't have enough names for autonaming.");
            return null;
        }

        return availableNames.get((int) (Math.random() * availableNames.size()));
    }

    public static void autonamePost() {
        SpiralIterator iterator = new SpiralIterator();
        while (iterator.hasNext()) {
            Post post = iterator.next();
            if (post.isNamed()) {
                continue;
            }

            String name = getRandom();

            if (name == null) {
                return;
            }

            PostApi.NamedPostApi.create(post.id(), name, false, null, true);
            Utils.executeCommandAsServer("/setworldspawn " + post.x() + " " + (post.y() + 1) + " " + post.z());

            // Broadcast to all players
            MinecraftServerSupplier.getServer().getPlayerManager().broadcast(
                    TelepostMessages.getMessage(null, "telepost.autonamed", Formatting.GREEN, name, post.getCoordinates()),
                    false
            );
            LOGGER.info("[Monthly Autonaming] Named post " + name + " at " + post.getCoordinates());
            return;
        }
        LOGGER.warn("Config file for Telepost doesn't have enough names for autonaming.");
    }
}
