package com.kryeit.telepost;

import com.kryeit.telepost.commands.PostCommands;
import com.kryeit.telepost.commands.TeleportHandler;
import com.kryeit.telepost.config.ConfigReader;
import com.kryeit.telepost.posts.PostBuilder;
import com.kryeit.telepost.storage.DatabaseUtils;
import com.mojang.logging.LogUtils;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.LevelResource;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

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


        NeoForge.EVENT_BUS.register(TeleportHandler.class);
        NeoForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        PostBuilder.copyStructures(event.getServer().getWorldPath(LevelResource.ROOT));
    }

    @SubscribeEvent
    public void onCommandRegistration(RegisterCommandsEvent event) {
        PostCommands.register(event.getDispatcher());
    }
}
