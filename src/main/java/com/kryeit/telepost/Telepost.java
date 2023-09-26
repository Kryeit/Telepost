package com.kryeit.telepost;

import com.kryeit.telepost.commands.*;
import com.kryeit.telepost.listeners.ServerTick;
import com.kryeit.telepost.storage.CommandDumpDB;
import com.kryeit.telepost.storage.IDatabase;
import com.kryeit.telepost.storage.LevelDBImpl;
import com.kryeit.telepost.storage.PlayerNamedPosts;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.minecraft.world.World;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class Telepost implements ModInitializer {

    public static Telepost instance;

    public IDatabase database;
    public PlayerNamedPosts playerNamedPosts;
    public static Map<UUID, UUID> invites = new HashMap<>();
    public static boolean postBuilding = false;

    @Override
    public void onInitialize() {
        initializeDatabases();
        instance = this;
        registerCommands();
        registerDisableEvent();
        registerEvents();
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
