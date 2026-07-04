package com.kryeit.telepost.posts;

import com.kryeit.telepost.storage.Database;

import java.util.Objects;
import java.util.UUID;

public record Relation(
        long id, UUID fromUUID, UUID toUUID,
        RelationType type
) {

    public static RelationType getRelation(UUID from, UUID to) {
        if (from == null || to == null) {
            return RelationType.NONE;
        }

        if (Objects.equals(from, to)) {
            return RelationType.ALLY;
        }

        return Database.getJdbi().withHandle(handle ->
                handle.createQuery("SELECT type FROM relations WHERE from_uuid = :fromUUID AND to_uuid = :toUUID")
                        .bind("fromUUID", from)
                        .bind("toUUID", to)
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
                handle.createUpdate("INSERT INTO relations (from_uuid, to_uuid, type) VALUES (:fromUUID, :toUUID, 'ENEMY') ON CONFLICT (from_uuid, to_uuid) DO UPDATE SET type = 'ENEMY'")
                        .bind("fromUUID", from)
                        .bind("toUUID", to)
                        .execute()
        );
    }

    public static void makeAlly(UUID from, UUID to) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("INSERT INTO relations (from_uuid, to_uuid, type) VALUES (:fromUUID, :toUUID, 'ALLY') ON CONFLICT (from_uuid, to_uuid) DO UPDATE SET type = 'ALLY'")
                        .bind("fromUUID", from)
                        .bind("toUUID", to)
                        .execute()
        );
    }

    public static void forgive(UUID from, UUID to) {
        Database.getJdbi().useHandle(handle ->
                handle.createUpdate("DELETE FROM relations WHERE from_uuid = :fromUUID AND to_uuid = :toUUID")
                        .bind("fromUUID", from)
                        .bind("toUUID", to)
                        .execute()
        );
    }

    public enum RelationType {
        ALLY, ENEMY, NONE
    }
}
