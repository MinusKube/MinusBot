package fr.minuskube.bot.discord.util;

import java.awt.*;

public class RGB {

    public static int fromRGB(int r, int g, int b) {
        return ((r & 0xFF) << 16) |
                ((g & 0xFF) << 8)  |
                ((b & 0xFF));
    }

    public static int fromColor(Color color) {
        return color != null    ? fromRGB(color.getRed(), color.getGreen(), color.getBlue())
                                : 0;
    }

}
