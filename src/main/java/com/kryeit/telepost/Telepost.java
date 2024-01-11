package com.kryeit.telepost;

import com.kryeit.telepost.commands.*;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.listeners.ServerTick;
import com.kryeit.telepost.post.StructureHandler;
import com.kryeit.telepost.storage.CommandDumpDB;
import com.kryeit.telepost.storage.IDatabase;
import com.kryeit.telepost.storage.LevelDBImpl;
import com.kryeit.telepost.storage.PlayerNamedPosts;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.World;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Telepost implements DedicatedServerModInitializer {

    public static Telepost instance;

    public static final Logger LOGGER = LoggerFactory.getLogger(Telepost.class);
    public IDatabase database;
    public PlayerNamedPosts playerNamedPosts;
    public static Map<UUID, UUID> invites = new HashMap<>();
    public static boolean postBuilding = false;

    @Override
    public void onInitializeServer() {
        initializeDatabases();
        instance = this;
        registerCommands();
        registerDisableEvent();
        registerEvents();

        StructureHandler.createStructures();

        if (CompatAddon.BLUE_MAP.isLoaded()) {
            try (FileReader reader = new FileReader("marker-file.json")) {
                BlueMapImpl.markerSet = MarkerGson.INSTANCE.fromJson(reader, MarkerSet.class);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            BlueMapImpl.updateMarkerSet();
        }

        try {
            LOGGER.info("Reading config file...");
            ConfigReader.readFile(Path.of("config/telepost"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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

            CommandDumpDB.register(dispatcher);
        });
    }

    public void registerEvents() {
        ServerTickEvents.END_SERVER_TICK.register(new ServerTick());
    }

    public void initializeDatabases() {
        database = new LevelDBImpl();
        try {
            playerNamedPosts = new PlayerNamedPosts("mods/telepost/PlayerPosts");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void registerDisableEvent() {
        ServerWorldEvents.UNLOAD.register((server, world) -> {
            if (world.getRegistryKey() == World.OVERWORLD) {
                database.stop();
            }
        });
    }

    public static Telepost getInstance() {
        return instance;
    }

    public static IDatabase getDB() {
        return getInstance().database;
    }
}
