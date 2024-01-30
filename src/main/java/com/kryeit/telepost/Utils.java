package com.kryeit.telepost;

import com.kryeit.telepost.offlines.Offlines;
import com.kryeit.telepost.post.GridIterator;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ChunkStatus;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static com.kryeit.telepost.post.Post.WORLD;

public class Utils {
    public static String nameToId(String name) {
        return name.replace(" ", ".").toLowerCase();
    }
    public static boolean isInOverworld(ServerPlayerEntity player) {
        return player.getWorld().equals(WORLD);
    }
    public static boolean isInvited(ServerPlayerEntity owner, ServerPlayerEntity invited) {
        return Telepost.invites.containsKey(invited.getUuid()) && Telepost.invites.get(invited.getUuid()).equals(owner.getUuid());
    }
    public static void loadChunk(int chunkX, int chunkZ) {
        ChunkManager chunkManager = WORLD.getChunkManager();
        chunkManager.getChunk(chunkX, chunkZ, ChunkStatus.FULL, true);
    }

    public static boolean isPostNamedByAdmin(NamedPost post) {
        return Telepost.getInstance().playerNamedPosts.getElement(post.id()) == null;
    }

    public static String getNamedPostOwner(NamedPost post) {
        UUID id = Telepost.getInstance().playerNamedPosts.getElement(post.id());

        if (id == null) return "Admin";

        return Offlines.getNameByUUID(id);
    }

    public static List<Post> getNonNamedPosts() {
        GridIterator iterator = new GridIterator();
        List<Post> posts = new ArrayList<>();

        if (iterator.hasNext()) {
            Vec3d loc = iterator.next();

            Post post = new Post(loc);
            if (!post.isNamed()) {
                posts.add(post);
            }
        }

        return posts;
    }

    public static void executeCommandAsServer(String command) {
        // Create a command source that represents the server
        ServerCommandSource source = MinecraftServerSupplier.getServer().getCommandSource();

        // Execute the command
        MinecraftServerSupplier.getServer().getCommandManager().executeWithPrefix(source, command);
    }
}
