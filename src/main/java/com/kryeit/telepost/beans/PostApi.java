package com.kryeit.telepost.beans;

import com.kryeit.telepost.Database;
import com.kryeit.telepost.MinecraftServerSupplier;
import com.kryeit.telepost.config.ConfigReader;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jdbi.v3.core.Jdbi;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class PostApi {
    public static final Jdbi jdbi = Database.getJdbi();
    public static final World OVERWORLD = MinecraftServerSupplier.getServer().getWorld(World.OVERWORLD);

    public static Optional<Post> getClosest(ServerPlayerEntity player) {

        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT * FROM posts
                    ORDER BY ABS(x - :x) + ABS(z - :z) 
                    LIMIT 1
                """)
                        .bind("x", player.getBlockX())
                        .bind("z", player.getBlockZ())
                        .mapToBean(Post.class)
                        .findFirst()
        );
    }

    public static Optional<Post> getClosest(Vec3d pos) {

        return jdbi.withHandle(handle ->
                handle.createQuery("""
                    SELECT * FROM posts
                    ORDER BY ABS(x - :x) + ABS(z - :z) 
                    LIMIT 1
                """)
                        .bind("x", pos.getX())
                        .bind("z", pos.getZ())
                        .mapToBean(Post.class)
                        .findFirst()
        );
    }



    public static Post create(int x, int z) {
        // Inner worldborder logic
        if (Math.abs(x) <= ConfigReader.PostSystem.INNER_WORLDBORDER &&
                Math.abs(z) <= ConfigReader.PostSystem.INNER_WORLDBORDER) {
            long clearance = ConfigReader.PostSystem.INNER_POST_CLEARANCE;
            int postX = (int) (x / clearance * clearance);
            int postZ = (int) (z / clearance * clearance);

            return jdbi.withHandle(handle -> {
                long generatedId = handle.createQuery("INSERT INTO posts (x, z) VALUES (?, ?) RETURNING id")
                        .mapTo(Long.class)
                        .first();
                return new Post(generatedId, postX, postZ);
            });
        }

        // Outer worldborder - allow exact location
        if (Math.abs(x) <= ConfigReader.PostSystem.OUTER_WORLDBORDER &&
                Math.abs(z) <= ConfigReader.PostSystem.OUTER_WORLDBORDER) {
            return jdbi.withHandle(handle -> {
                long generatedId = handle.createQuery("INSERT INTO posts (x, z) VALUES (?, ?) RETURNING id")
                        .mapTo(Long.class)
                        .first();
                return new Post(generatedId, x, z);
            });
        }

        // Outside both worldborders
        throw new IllegalArgumentException("Location is outside worldborders");
    }

    public static boolean delete(long id) {
        return jdbi.withHandle(handle ->
                handle.execute("DELETE FROM posts WHERE id = ?", id)
        ) > 0;
    }

    public static Optional<Post> get(long id) {
        return jdbi.withHandle(handle ->
                handle.createQuery("SELECT * FROM post WHERE id = ?")
                        .bind(0, id)
                        .mapToBean(Post.class)
                        .findFirst()
        );
    }

    public static class NamedPostApi {
        public static boolean create(long id, String name, boolean privated, UUID player, boolean isAdmin) {
            return jdbi.withHandle(handle ->
                    handle.execute(
                            "INSERT INTO named (id, name, privated, player, is_admin) VALUES (?, ?, ?, ?, ?)",
                            id, name, privated, player, isAdmin
                    )
            ) > 0;
        }

        public static List<NamedPost> get() {
            return jdbi.withHandle(handle ->
                    handle.createQuery("SELECT * FROM named")
                            .mapToBean(NamedPost.class)
                            .list()
            );
        }

        public static Optional<NamedPost> get(ServerPlayerEntity player) {
            return jdbi.withHandle(handle ->
                    handle.createQuery("SELECT * FROM named WHERE player = ?")
                            .bind(0, player.getUuid())
                            .mapToBean(NamedPost.class)
                            .findFirst()
            );
        }

        public static Optional<NamedPost> get(String name) {
            return jdbi.withHandle(handle ->
                    handle.createQuery("SELECT * FROM named WHERE name = ?")
                            .bind(0, name)
                            .mapToBean(NamedPost.class)
                            .findFirst()
            );
        }

        public static boolean has(ServerPlayerEntity player) {
            return jdbi.withHandle(handle ->
                    handle.createQuery("SELECT COUNT(*) FROM named WHERE player = ?")
                            .bind(0, player.getUuid())
                            .mapTo(Integer.class)
                            .one()
            ) > 0;
        }

        public static void delete(long id) {
            jdbi.withHandle(handle ->
                    handle.execute("DELETE FROM named WHERE id = ?", id)
            );
        }

        public static boolean transfer(long id, UUID newPlayer) {
            return jdbi.withHandle(handle ->
                    handle.execute("UPDATE named SET player = ? WHERE id = ?", newPlayer, id)
            ) > 0;
        }

        public static String getName(long id) {
            return jdbi.withHandle(handle ->
                    handle.createQuery("SELECT name FROM named WHERE id = ?")
                            .bind(0, id)
                            .mapTo(String.class)
                            .one()
            );
        }
    }

    public static class HomePostApi {
        public static void create(ServerPlayerEntity player) {
            UUID uuid = player.getUuid();
            Optional<Post> closestPost = getClosest(player);

            closestPost.ifPresent(post ->
                    jdbi.withHandle(handle ->
                            handle.execute("INSERT INTO homes (player, post_id) VALUES (?, ?) " +
                                            "ON CONFLICT (player) DO UPDATE SET post_id = ?",
                                    uuid, post.id(), post.id())
                    )
            );
        }

        public static Optional<HomePost> get(UUID uuid) {
            return jdbi.withHandle(handle ->
                    handle.createQuery("SELECT * FROM homes WHERE player = ?")
                            .bind(0, uuid)
                            .mapToBean(HomePost.class)
                            .findFirst()
            );
        }
    }
}