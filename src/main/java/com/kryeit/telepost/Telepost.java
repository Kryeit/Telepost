package com.kryeit.telepost;

import com.kryeit.telepost.commands.*;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.listeners.ServerTick;
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
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.World;

import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Telepost implements DedicatedServerModInitializer {

    public static Telepost instance;

    public IDatabase database;
    public PlayerNamedPosts playerNamedPosts;
    public static Map<UUID, UUID> invites = new HashMap<>();
    public static boolean postBuilding = false;
    public static ServerPlayerEntity player = null;

    @Override
    public void onInitializeServer() {
        initializeDatabases();
        instance = this;
        registerCommands();
        registerDisableEvent();
        registerEvents();

        if (CompatAddon.BLUE_MAP.isLoaded()) {
            try (FileReader reader = new FileReader("marker-file.json")) {
                BlueMapImpl.markerSet = MarkerGson.INSTANCE.fromJson(reader, MarkerSet.class);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
            BlueMapImpl.updateMarkerSet();
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
            playerNamedPosts = new PlayerNamedPosts("Telepost/PlayerPosts");
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
