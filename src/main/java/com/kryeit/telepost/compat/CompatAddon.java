package com.kryeit.telepost.compat;

import net.fabricmc.loader.api.FabricLoader;

import java.util.Optional;
import java.util.function.Supplier;

public enum CompatAddon {
    GRIEF_DEFENDER("GriefDefender"),
    WORLD_EDIT("WorldEdit");

    private final String id;

    CompatAddon(String id) {
        this.id = id;
    }

    public boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(id());
    }

    public <T> Optional<T> getIfInstalled(Supplier<Supplier<T>> executable) {
        return isLoaded() ? Optional.ofNullable(executable.get().get()) : Optional.empty();
    }

    public void runIfInstalled(Supplier<Runnable> executable) {
        if (isLoaded()) executable.get().run();
    }

    public String id() {
        return id;
    }
}
