package com.kryeit.telepost.storage.bytes;

import net.minecraft.util.math.Vec3d;
import org.jetbrains.annotations.NotNull;

public record NamedPost(String id, String name, Vec3d location) implements Comparable<NamedPost> {
    public static NamedPost fromBytes(String id, ReadableByteArray data) {
        String name = data.readString();
        Vec3d position = data.readLocation();
        return new NamedPost(id, name, position);
    }

    public byte[] toBytes() {
        WritableByteArray data = new WritableByteArray();
        data.writeString(name);
        data.writeLocation(location);
        return data.toByteArray();
    }

    @Override
    public int compareTo(@NotNull NamedPost o) {
        return name().compareTo(o.name());
    }
}
