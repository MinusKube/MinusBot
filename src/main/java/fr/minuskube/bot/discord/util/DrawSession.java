package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.net.URL;

public class DrawSession {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrawSession.class);

    private final int width, height;

    private BufferedImage lastImg;
    private BufferedImage image;

    private Graphics2D g2d;

    private final MessageChannel channel;
    private Message lastMsg;

    public DrawSession(int width, int height, MessageChannel channel) {
        this.channel = channel;

        this.width = width;
        this.height = height;

        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        this.g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.BLACK);
    }

    public void drawLine(int x1, int y1, int x2, int y2, int lineWidth) {
        copyLastImg();

        Stroke def = g2d.getStroke();

        g2d.setStroke(new BasicStroke(lineWidth));
        g2d.drawLine(x1, y1, x2, y2);

        g2d.setStroke(def);
    }

    public void drawLine(int x1, int y1, int x2, int y2) {
        copyLastImg();

        g2d.drawLine(x1, y1, x2, y2);
    }

    public void drawRect(int x, int y, int width, int height, int lineWidth) {
        copyLastImg();

        Stroke def = g2d.getStroke();

        g2d.setStroke(new BasicStroke(lineWidth));
        g2d.drawRect(x, y, width, height);

        g2d.setStroke(def);
    }

    public void drawRect(int x, int y, int width, int height) {
        copyLastImg();

        g2d.drawRect(x, y, width, height);
    }

    public void fillRect(int x, int y, int width, int height) {
        copyLastImg();

        g2d.fillRect(x, y, width, height);
    }

    public void drawCircle(int x, int y, int width, int height, int lineWidth) {
        copyLastImg();

        Stroke def = g2d.getStroke();

        g2d.setStroke(new BasicStroke(lineWidth));
        g2d.drawOval(x, y, width, height);

        g2d.setStroke(def);
    }

    public void drawCircle(int x, int y, int width, int height) {
        copyLastImg();

        g2d.drawOval(x, y, width, height);
    }

    public void fillCircle(int x, int y, int width, int height) {
        copyLastImg();

        g2d.fillOval(x, y, width, height);
    }

    public void drawString(float size, int x, int y, String text) {
        copyLastImg();

        g2d.setFont(g2d.getFont().deriveFont(size));
        g2d.drawString(text, x, y + g2d.getFontMetrics().getAscent());
    }

    public void drawImage(int x, int y, String url, int width, int height) throws IOException {
        Image img = ImageIO.read(new URL(url));
        copyLastImg();

        g2d.drawImage(img, x, y, width, height, null, null);
    }

    public void drawImage(int x, int y, String url) throws IOException {
        Image img = ImageIO.read(new URL(url));
        copyLastImg();

        g2d.drawImage(img, x, y, null, null);
    }

    public void color(int red, int green, int blue, int alpha) {
        g2d.setColor(new Color(red, green, blue, alpha));
    }

    public void sendColor() {
        BufferedImage image = new BufferedImage(30, 30, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g2d = image.createGraphics();

        g2d.setColor(this.g2d.getColor());
        g2d.fillRect(0, 0, 50, 50);

        if(channel.getType() == ChannelType.TEXT) {
            TextChannel tc = (TextChannel) channel;
            Guild guild = tc.getGuild();

            if(!guild.getSelfMember().hasPermission(tc, Permission.MESSAGE_ATTACH_FILES)) {
                channel.sendMessage(new MessageBuilder()
                        .append("No permission to send files!", MessageBuilder.Formatting.BOLD).build())
                        .queue();
                return;
            }
        }

        try {
            File tempFile = StreamUtils.tempFileFromImage(image, "draw-color-" + channel.getId(), ".png");
            channel.sendFile(tempFile, new MessageBuilder().append("New color set!").build()).queue();
        } catch(IOException e) {
            LOGGER.error("Couldn't send color image:", e);
        }
    }

    private void copyLastImg() {
        ColorModel cm = image.getColorModel();
        boolean alphaPremult = cm.isAlphaPremultiplied();

        this.lastImg = new BufferedImage(cm, image.copyData(null), alphaPremult, null);
        this.lastImg.createGraphics().setColor(g2d.getColor());
    }

    public boolean cancel() {
        if(this.lastImg == null)
            return false;

        this.image = this.lastImg;

        this.g2d = this.image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.BLACK);

        this.lastImg = null;

        return true;
    }

    public void reset() {
        this.image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        this.g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setFont(new Font("Arial", Font.PLAIN, 20));
        g2d.setColor(Color.BLACK);
    }

    public void send() {
        if(lastMsg != null)
            lastMsg.delete().queue();

        if(channel.getType() == ChannelType.TEXT) {
            TextChannel tc = (TextChannel) channel;
            Guild guild = tc.getGuild();

            if(!guild.getSelfMember().hasPermission(tc, Permission.MESSAGE_ATTACH_FILES)) {
                channel.sendMessage(new MessageBuilder()
                        .append("No permission to send files!", MessageBuilder.Formatting.BOLD).build())
                        .queue();
                return;
            }
        }

        try {
            File tempFile = StreamUtils.tempFileFromImage(image, "draw-session-" + channel.getId(), ".png");
            this.lastMsg = channel.sendFile(tempFile, null).complete();
        } catch(IOException e) {
            LOGGER.error("Couldn't send image:", e);
        }
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }

    public MessageChannel getChannel() { return channel; }

}
