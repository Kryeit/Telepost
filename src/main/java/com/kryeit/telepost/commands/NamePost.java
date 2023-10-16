package com.kryeit.telepost.commands;

import com.flowpowered.math.vector.Vector3d;
import com.griefdefender.api.GriefDefender;
import com.griefdefender.api.claim.Claim;
import com.griefdefender.api.claim.TrustTypes;
import com.kryeit.telepost.Telepost;
import com.kryeit.telepost.TelepostMessages;
import com.kryeit.telepost.TelepostPermissions;
import com.kryeit.telepost.Utils;
import com.kryeit.telepost.compat.BlueMapImpl;
import com.kryeit.telepost.compat.CompatAddon;
import com.kryeit.telepost.compat.GriefDefenderImpl;
import com.kryeit.telepost.post.Post;
import com.kryeit.telepost.storage.bytes.NamedPost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import de.bluecolored.bluemap.api.BlueMapAPI;
import de.bluecolored.bluemap.api.BlueMapMap;
import de.bluecolored.bluemap.api.gson.MarkerGson;
import de.bluecolored.bluemap.api.markers.POIMarker;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

import static com.kryeit.telepost.compat.GriefDefenderImpl.NEEDED_CLAIMBLOCKS;
import static com.kryeit.telepost.post.Post.WORLD;

public class NamePost {
    public static int execute(CommandContext<ServerCommandSource> context, String name) throws IOException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayer();

        if (player == null || !Utils.isInOverworld(player)) {
            Supplier<Text> message = () -> Text.translatable("telepost.no_permission");
            source.sendFeedback(message, false);
            return 0;
        }

        Post post = new Post(player.getPos());

        Text text;

        // Check if nearest named
        if (post.isNamed()) {
            text = TelepostMessages.getMessage(player, "telepost.already_named", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        // Check if name is in use
        Optional<NamedPost> namedPost = Telepost.getDB().getNamedPost(Utils.nameToId(name));
        if (namedPost.isPresent()) {
            text = TelepostMessages.getMessage(player, "telepost.already_named", Formatting.RED);
            player.sendMessage(text, true);
            return 0;
        }

        if (!TelepostPermissions.isAdmin(player) && CompatAddon.GRIEF_DEFENDER.isLoaded()) {
            if (GriefDefenderImpl.getClaimBlocks(player.getUuid()) < NEEDED_CLAIMBLOCKS) {
                text = TelepostMessages.getMessage(player, "telepost.name.claimblocks", Formatting.RED, NEEDED_CLAIMBLOCKS);
                player.sendMessage(text);
                return 0;
            }
            Telepost.getInstance().playerNamedPosts.addElement(name, player.getUuid());

            Claim claim = GriefDefender.getCore().getClaimAt(post.getPos());
            if (claim != null && claim.isAdminClaim()) {
                claim.addUserTrust(player.getUuid(), TrustTypes.MANAGER);
            }
        }

        if (CompatAddon.BLUE_MAP.isLoaded()) {
            POIMarker marker = POIMarker.builder()
                    .label(name)
                    .position(new Vector3d(post.getX(), post.getY(), post.getZ()))
                    .build();
            BlueMapImpl.markerSet.put(name, marker);

            try (FileWriter writer = new FileWriter("marker-file.json")) {
                MarkerGson.INSTANCE.toJson(BlueMapImpl.markerSet, writer);
            } catch (IOException ex) {
                ex.printStackTrace();
            }

            BlueMapImpl.updateMarkerSet();
        }
        Telepost.getDB().addNamedPost(new NamedPost(Utils.nameToId(name), name, post.getPos()));

        text = TelepostMessages.getMessage(player, "telepost.named", Formatting.GREEN, name, post.getStringCoords());
        player.sendMessage(text);

        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("namepost")
                .then(CommandManager.argument("name", StringArgumentType.word())
                        .executes(context -> {
                            try {
                                return execute(context, StringArgumentType.getString(context, "name"));
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }
                        })
                )
        );
    }
}
