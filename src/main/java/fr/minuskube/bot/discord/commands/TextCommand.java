package fr.minuskube.bot.discord.commands;

import com.google.common.collect.ImmutableMap;
import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.StreamUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;

public class TextCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(TextCommand.class);
    private static final Map<String, Color> COLORS = ImmutableMap.<String, Color>builder()
            .put("red", Color.RED)
            .put("green", Color.GREEN)
            .put("blue", Color.BLUE)
            .put("cyan", Color.CYAN)
            .put("black", Color.BLACK)
            .put("white", Color.WHITE)
            .put("orange", Color.ORANGE)
            .put("yellow", Color.YELLOW)
            .put("gray", Color.GRAY)
            .put("grey", Color.GRAY)
            .put("magenta", Color.MAGENTA)
            .put("pink", Color.PINK)
            .put("darkgray", Color.DARK_GRAY)
            .put("darkgrey", Color.DARK_GRAY)
            .put("transparent", new Color(0, 0, 0, 0))
            .build();

    public TextCommand() {
        super("text", Collections.emptyList(), "Sends a image with a colorable text.",
                "<size> <color> <backgroundColor> <text>");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();

        int size;

        try {
            size = Integer.parseInt(args[0]);

            if(size <= 0) {
                MessageUtils.error(channel, "The given size must be over 0.").queue();
                return;
            }
        } catch(NumberFormatException e) {
            MessageUtils.error(channel, "The given size is not a correct number.").queue();
            return;
        }

        Color textColor = COLORS.get(args[1].toLowerCase());
        Color backgroundColor = COLORS.get(args[2].toLowerCase());

        if(textColor == null || backgroundColor == null) {
            MessageUtils.error(channel, "Unknown color given. Available colors:\n" +
                    String.join(", ", COLORS.keySet())).queue();
            return;
        }

        String text = StringUtils.join(ArrayUtils.removeAll(args, 0, 1, 2), " ");
        sendImage(channel, size, text, textColor, backgroundColor);

        msg.delete().queue();
    }

    private void sendImage(MessageChannel channel, int size, String text, Color textColor, Color backgroundColor) {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        Font font = new Font("Arial", Font.PLAIN, size);
        FontMetrics metrics = image.getGraphics().getFontMetrics(font);

        String[] lines = WordUtils.wrap(text, 60).split("\\n");
        int maxWidth = Arrays.stream(lines)
                .map(metrics::stringWidth)
                .max(Comparator.naturalOrder())
                .orElse(0);

        image = new BufferedImage(maxWidth + 20, lines.length * (metrics.getHeight() + 5) + 5,
                BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(font);

        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());

        if(textColor.getAlpha() == 0)
            g2d.setComposite(AlphaComposite.SrcIn);

        g2d.setColor(textColor);

        for(int i = 0; i < lines.length; i++) {
            String line = lines[i];

            Rectangle2D bounds = metrics.getStringBounds(line, g2d);

            int x = 10;
            int y = 5 + i * (metrics.getHeight() + 5) + metrics.getAscent();

            g2d.drawString(line, x, y);
        }

        try {
            File tempFile = StreamUtils.tempFileFromImage(image, "potato", ".png");
            channel.sendFile(tempFile, null).queue();
        } catch(IOException e) {
            LOGGER.error("Couldn't send image: ", e);
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return args.length >= 4; }

}
