package com.kryeit.telepost;

import com.kryeit.telepost.autonaming.MonthlyCheckRunnable;
import com.kryeit.telepost.commands.*;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.listeners.ServerTick;
import com.kryeit.telepost.post.StructureHandler;
import com.kryeit.telepost.storage.CommandDumpDB;
import com.kryeit.telepost.storage.IDatabase;
import com.kryeit.telepost.storage.LevelDBImpl;
import com.kryeit.telepost.storage.NamedPostStorage;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Telepost implements DedicatedServerModInitializer {

    public static Telepost instance;
    public static final String ID = "telepost";
    public static final String NAME = "Telepost";

    public static final Logger LOGGER = LoggerFactory.getLogger(NAME);
    public IDatabase database;
    public NamedPostStorage playerNamedPosts;
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

        initializeDatabases();
        instance = this;
        registerCommands();
        registerDisableEvent();
        registerEvents();
        registerMonthlyCheck();

        StructureHandler.createStructures();

        if (CompatAddon.BLUE_MAP.isLoaded()) {
            LOGGER.info("BlueMap is loaded, loading marker set from file...");
            File directory = new File("mods/" + ID);
            if (!directory.exists())
                directory.mkdirs();

            File file = new File(directory, "marker-file.json");
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }
            }

            try (FileReader reader = new FileReader(file)) {
                BlueMapImpl.markerSet = MarkerGson.INSTANCE.fromJson(reader, MarkerSet.class);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            BlueMapImpl.updateMarkerSet();
            LOGGER.info("BlueMap loaded successfully");
        }
    }

    public void registerMonthlyCheck() {
        if (!ConfigReader.AUTONAMING) return;

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        MonthlyCheckRunnable monthCheckRunnable = new MonthlyCheckRunnable();

        // Schedule to run every hour
        executor.scheduleAtFixedRate(monthCheckRunnable, 0, 1, TimeUnit.HOURS);
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

            CommandDumpDB.register(dispatcher);
        });
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(new ServerTick());
    }

    public void initializeDatabases() {
        // Database of all posts and homeposts, with their locations and such
        database = new LevelDBImpl();

        // Database of simple POST_ID -> PLAYER_ID mappings for QoL additions and Server Owner convenience
        try {
            playerNamedPosts = new NamedPostStorage("mods/" + ID +"/PlayerPosts");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerDisableEvent() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            database.stop();
        });
    }

    public static Telepost getInstance() {
        return instance;
    }

    public static IDatabase getDB() {
        return getInstance().database;
    }
}
