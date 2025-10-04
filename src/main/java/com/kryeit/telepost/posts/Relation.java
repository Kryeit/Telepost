package com.kryeit.telepost.posts;

import com.kryeit.telepost.storage.Database;

import java.util.UUID;

public record Relation(
        long id, UUID from, UUID to,
        RelationType type
) {

    public static RelationType getRelation(UUID from, UUID to) {
        if (from.equals(to)) {
            return RelationType.ALLY;
        }

        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT type FROM relations WHERE from = :from AND to = :to")
                        .bind("from", from)
                        .bind("to", to)
                        .map((rs, ctx) -> {
                            String type = rs.getString("type");
                            return type != null ? RelationType.valueOf(type) : null;
                        })
                        .findOne()
                        .orElse(null)
        );
    }

    public static void makeEnemy(UUID from, UUID to) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("INSERT INTO relations (from, to, type) VALUES (:from, :to, 'ENEMY') ON CONFLICT (from, to) DO UPDATE SET type = 'ENEMY'")
                        .bind("from", from)
                        .bind("to", to)
                        .execute()
        );
    }

    public static void makeAlly(UUID from, UUID to) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("INSERT INTO relations (from, to, type) VALUES (:from, :to, 'ALLY') ON CONFLICT (from, to) DO UPDATE SET type = 'ALLY'")
                        .bind("from", from)
                        .bind("to", to)
                        .execute()
        );
    }

    public static void forgive(UUID from, UUID to) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("DELETE FROM relations WHERE from = :from AND to = :to")
                        .bind("from", from)
                        .bind("to", to)
                        .execute()
        );
    }

    public enum RelationType {
        ALLY, ENEMY, NONE
    }
}
