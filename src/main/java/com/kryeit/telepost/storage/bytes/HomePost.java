package com.kryeit.telepost.storage.bytes;

import net.minecraft.util.math.Vec3d;

import java.util.UUID;

public record HomePost(UUID playerID, Vec3d location) {
    public static HomePost fromBytes(ReadableByteArray data) {
        return new HomePost(data.readUUID(), data.readLocation());
    }

    public byte[] toBytes() {
        WritableByteArray data = new WritableByteArray();
        data.writeUUID(playerID());
        data.writeLocation(location());
        return data.toByteArray();
    }
}
