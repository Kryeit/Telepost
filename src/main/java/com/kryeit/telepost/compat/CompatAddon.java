package com.kryeit.telepost.compat;

import net.fabricmc.loader.api.FabricLoader;

public enum CompatAddon {
    GRIEF_DEFENDER("griefdefender"),
    WORLD_EDIT("worldedit"),
    BLUE_MAP("bluemap");

    private final String id;

    CompatAddon(String id) {
        this.id = id;
    }

    public boolean isLoaded() {
        return FabricLoader.getInstance().isModLoaded(id());
    }

    public String id() {
        return id;
    }
}
