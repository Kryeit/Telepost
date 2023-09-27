package com.kryeit.telepost;

import net.minecraft.text.MutableText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class TelepostMessages {

    public static Text getMessage(String key, Formatting color, Object... args) {
        String[] parts = Text.translatable(key).getContent().toString().split("%s", -1);

        if (parts.length == 0) {
            return Text.translatable(key);
        }

        MutableText result = Text.literal("");

        for (int i = 0; i < parts.length; i++) {
            if (i < args.length) {
                result.append(Text.literal(parts[i]).setStyle(Style.EMPTY.withFormatting(color)));
                result.append(Text.literal(args[i].toString()).setStyle(Style.EMPTY.withFormatting(Formatting.GOLD)));
            } else {
                result.append(Text.literal(parts[i]).setStyle(Style.EMPTY.withFormatting(color)));
            }
        }

        return result;
    }
}
