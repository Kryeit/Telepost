package com.kryeit.telepost;

import com.kryeit.telepost.autonaming.MonthlyCheckRunnable;
import com.kryeit.telepost.commands.*;
import com.kryeit.telepost.commands.cooldown.CooldownStorage;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.listeners.ServerTick;
import com.kryeit.telepost.post.StructureHandler;
import com.kryeit.telepost.storage.IDatabase;
import com.kryeit.telepost.storage.LevelDBImpl;
import com.kryeit.telepost.storage.NamedPostStorage;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    public static NamedPostStorage playerNamedPosts;
    public static Map<UUID, UUID> invites = new HashMap<>();

    public static CooldownStorage randomPostCooldown;
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

        // Comment this out in dev environment
        StructureHandler.createStructures();

        if (CompatAddon.BLUEMAP.isLoaded()) {
            LOGGER.info("BlueMap is loaded, loading marker set from file...");
            BlueMapImpl.loadMarkerSet();
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
            RandomPost.register(dispatcher);

            //CommandDumpDB.register(dispatcher);
        });
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(new ServerTick());
    }

    public void initializeDatabases() {
        // Database of all posts and homeposts, with their locations and such
        database = new LevelDBImpl();

        // For /randompost cooldown
        try {
            randomPostCooldown = new CooldownStorage("mods/" + ID + "/randompostcooldown");
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (CompatAddon.GRIEF_DEFENDER.isLoaded())
            playerNamedPosts = new NamedPostStorage();
    }

    public void registerDisableEvent() {
        ServerLifecycleEvents.SERVER_STOPPING.register(server -> {
            database.stop();

            if (CompatAddon.GRIEF_DEFENDER.isLoaded())
                playerNamedPosts.close();
        });
    }

    public static Telepost getInstance() {
        return instance;
    }

    public static IDatabase getDB() {
        return getInstance().database;
    }
}
