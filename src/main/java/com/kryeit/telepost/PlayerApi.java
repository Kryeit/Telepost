package com.kryeit.telepost;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;

import java.util.Optional;
import java.util.UUID;

public class PlayerApi {

    private final LuckPerms luckPerms;

    public PlayerApi() {
        this.luckPerms = LuckPermsProvider.get();
    }

    public UUID getUuidFromName(String name) {
        User user = luckPerms.getUserManager().getUser(name);
        if (user != null) {
            return user.getUniqueId();
        }
        return null;
    }

    public String getNameFromUuid(UUID uuid) {
        Optional<User> userOptional = Optional.ofNullable(luckPerms.getUserManager().loadUser(uuid).join());
        return userOptional.map(User::getUsername).orElse(null);
    }

    public boolean check(UUID uuid, String permission) {
        User user = luckPerms.getUserManager().getUser(uuid);
        return user != null && user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }
}
