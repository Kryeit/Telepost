package com.kryeit.telepost;

import com.kryeit.telepost.commands.PostCommands;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.posts.PostBuilder;
import com.kryeit.telepost.storage.Database;
import com.kryeit.telepost.storage.DatabaseUtils;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;

import java.io.IOException;
import java.nio.file.Path;

@Mod(Telepost.MODID)
public class Telepost {
    public static final String MODID = "telepost";

    public Telepost() {

        try {
            ConfigReader.readFile(Path.of("config/" + MODID));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        DatabaseUtils.createTables();


        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        PostBuilder.copyStructures(event.getServer().getWorldPath(LevelResource.ROOT));
    }

    @SubscribeEvent
    public void onServerStopped(ServerStoppedEvent event) {
        Database.close();
    }

    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event) {
        PostCommands.register(event.getDispatcher());
    }
}
