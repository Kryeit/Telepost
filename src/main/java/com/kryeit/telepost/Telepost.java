package com.kryeit.telepost;

import com.kryeit.telepost.autonaming.MonthlyCheckRunnable;
import com.kryeit.telepost.beans.PostApi;
import com.kryeit.telepost.commands.*;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.listeners.ServerTick;
import com.kryeit.telepost.post.StructureHandler;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.UUID;

import static com.kryeit.telepost.compat.BlueMapImpl.markerSet;

public class Telepost implements DedicatedServerModInitializer {

    private static final Timer MONTHLY_TIMER = new Timer(true);

    public static Telepost instance;
    public static final String ID = "telepost";
    public static final String NAME = "Telepost";

    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);

    public static Map<UUID, UUID> invites = new HashMap<>();

    public static boolean postBuilding = false;

    @Override
    public void onInitializeServer() {
        try {
            LOGGER.info("Reading config file...");
            ConfigReader.readFile(Path.of("config/" + ID));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        instance = this;
        registerCommands();
        registerDisableEvent();
        registerEvents();
        registerMonthlyCheck();

        // Comment this out in dev environment
        StructureHandler.createStructures();
    }

    public void registerMonthlyCheck() {
        if (!ConfigReader.AUTO_NAMING) return;

        long interval = Duration.ofHours(1).toMillis();
        MONTHLY_TIMER.schedule(new MonthlyCheckRunnable(), interval, interval);
    }

    public void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicatedServer, commandFunction) -> {
            NearestPost.register(dispatcher);
            SetHome.register(dispatcher);
            Home.register(dispatcher);
            NamePost.register(dispatcher);
            UnnamePost.register(dispatcher);
            Invite.register(dispatcher);
            Visit.register(dispatcher);
            BuildPosts.register(dispatcher);
            PostList.register(dispatcher);
            ForceVisit.register(dispatcher);
            PrivatePost.register(dispatcher);

            // IMPORTANT: Enable only for Kryeit builds
            //PostHelp.register(dispatcher);

            if (CompatAddon.GRIEF_DEFENDER.isLoaded()) {
                DeletePostClaim.register(dispatcher);
            }
        });
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(new ServerTick());

        if (CompatAddon.BLUEMAP.isLoaded()) {
            BlueMapAPI.onEnable(api ->
                    api.getWorld(PostApi.OVERWORLD).ifPresent(blueWorld -> {
                        BlueMapImpl.loadMarkerSet();
                        for (BlueMapMap map : blueWorld.getMaps()) {
                            map.getMarkerSets().put("posts", markerSet);
                        }
                    })
            );
        }
    }

    public void registerDisableEvent() {
        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            Database.closeDataSource();
        });
    }

    public static Telepost getInstance() {
        return instance;
    }
}
