package com.kryeit.telepost;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import xyz.nucleoid.server.translations.api.Localization;

public class TelepostMessages {

    public static Text getMessage(ServerPlayerEntity player, String key, Formatting color, Object... args) {
        String translation = Localization.raw(key, player);
        if (translation == null) return Text.of("");
        String[] parts = translation.split("%s", -1);

        if (parts.length == 0) {
            return Text.translatable(key);
        }

        MutableText result = Text.literal("");

        for (int i = 0; i < parts.length; i++) {
            if (i < args.length) {
                result.append(Text.literal(parts[i]).formatted(color));
                result.append(Text.literal(args[i].toString()).formatted(Formatting.GOLD));
            } else {
                result.append(Text.literal(parts[i]).formatted(color));
            }
        }

        return result;
    }
}
