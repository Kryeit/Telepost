package com.kryeit.telepost.storage;

import com.kryeit.telepost.Telepost;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class CommandDumpDB {

    public static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Supplier<Text> message = () -> Text.literal(Telepost.getDB().dump());
        source.sendFeedback(message, false);
        return Command.SINGLE_SUCCESS;
    }

    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("dumpdb")
                .executes(CommandDumpDB::execute)
        );
    }
}
