package com.kryeit.telepost.db_migrator.storage.bytes;

import com.kryeit.telepost.db_migrator.storage.Vec3d;

import java.nio.ByteBuffer;
import java.util.UUID;

public class ReadableByteArray {
    private final byte[] data;
    private int position = 0;

    public ReadableByteArray(byte[] data) {
        this.data = data;
    }

    public byte readByte() {
        return data[position++];
    }

    public int readInt() {
        return (readByte() << 24) | (readByte() << 16) | (readByte() << 8) | readByte();
    }

    public String readString() {
        int length = readInt();
        StringBuilder resultBuilder = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            resultBuilder.append((char) readByte());
        }
        return resultBuilder.toString();
    }

    public Vec3d readLocation() {
        double x = readDouble();
        double y = readDouble();
        double z = readDouble();
        return new Vec3d(x, y, z);
    }

    public double readDouble() {
        ByteBuffer byteBuffer = ByteBuffer.allocate(Double.BYTES);
        byteBuffer.put(readBytes(Double.BYTES));
        byteBuffer.flip();
        return byteBuffer.getDouble();
    }

    public long readLong() {
        return (long) (readByte() & 0xFF) << 56
                | (long) (readByte() & 0xFF) << 48
                | (long) (readByte() & 0xFF) << 40
                | (long) (readByte() & 0xFF) << 32
                | (long) (readByte() & 0xFF) << 24
                | (long) (readByte() & 0xFF) << 16
                | (long) (readByte() & 0xFF) << 8
                | (readByte() & 0xFF);
    }

    public byte[] readBytes(int length) {
        byte[] out = new byte[length];
        for (int i = 0; i < length; i++) out[i] = readByte();
        return out;
    }

    public UUID readUUID() {
        return new UUID(readLong(), readLong());
    }

    public boolean readBoolean() {
        return data[position++] != 0;
    }

    public boolean hasRemaining() {
        return position < data.length;
    }
}
