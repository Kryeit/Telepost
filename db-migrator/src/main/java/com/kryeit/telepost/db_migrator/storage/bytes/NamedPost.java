package com.kryeit.telepost.db_migrator.storage.bytes;

import com.kryeit.telepost.db_migrator.storage.Vec3d;

public record NamedPost(String id, String name, Vec3d location, boolean isPrivate) implements Comparable<NamedPost> {
    public static NamedPost fromBytes(String id, ReadableByteArray data) {
        String name = data.readString();
        Vec3d position = data.readLocation();
        boolean isPrivate = data.hasRemaining() && data.readBoolean();
        return new NamedPost(id, name, position, isPrivate);
    }

    public byte[] toBytes() {
        WritableByteArray data = new WritableByteArray();
        data.writeString(name);
        data.writeLocation(location);
        data.writeBoolean(isPrivate);
        return data.toByteArray();
    }

    @Override
    public int compareTo(NamedPost o) {
        return name().compareTo(o.name());
    }
}