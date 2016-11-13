package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.util.StreamUtils;
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
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.text.SimpleDateFormat;
import java.util.Locale;

import static java.awt.image.BufferedImage.TYPE_INT_ARGB;

public class TestImage {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestImage.class);

    private TextChannel channel;
    private Card card;
    private Member member;
    private boolean add;

    public TestImage(TextChannel channel, Card card, Member member, boolean add) {
        this.channel = channel;
        this.card = card;
        this.member = member;
        this.add = add;
    }

    public void sendImage() {
        final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM, HH:mm", Locale.ENGLISH);

        BufferedImage image = new BufferedImage(144, 68, TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_LCD_VRGB);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        g2d.setColor(new Color(210, 210, 210));
        g2d.fill(new RoundRectangle2D.Float(0, 0, image.getWidth(), image.getHeight(),
                8, 8));

        g2d.setColor(new Color(180, 180, 180));
        g2d.fill(new RoundRectangle2D.Float(0, 0, image.getWidth(), 14,
                8, 8));

        g2d.clipRect(0, 7, image.getWidth(), 7);
        g2d.fill(new Rectangle2D.Float(0, 7, image.getWidth(), 7));

        g2d.setClip(null);

        g2d.setColor(Color.BLACK);
        g2d.setFont(new Font("Tahoma", Font.PLAIN, 10));
        g2d.drawString(card.getName(), 4, 10);

        g2d.setColor(add    ? new Color(25, 200, 40)
                            : new Color(200, 25, 40));
        g2d.setStroke(new BasicStroke(4));

        g2d.drawLine(40 - 10, 41, 40 + 10, 41);
        if(add) g2d.drawLine(40, 41 - 10, 40, 41 + 10);

        if(member.getAvatarHash() != null) {
            try {
                URL url = new URL(member.getAvatarURL());
                URLConnection connection = url.openConnection();

                connection.setRequestProperty("User-Agent", "Mozilla/5.0");
                connection.connect();

                BufferedImage avatar = ImageIO.read(connection.getInputStream());

                float wr = 44f / avatar.getWidth();
                float wh = 44f / avatar.getHeight();
                float ratio = Math.min(wr, wh);

                Image imgAvatar = avatar.getScaledInstance((int) (avatar.getWidth() * ratio),
                        (int) (avatar.getHeight() * ratio), Image.SCALE_SMOOTH);

                g2d.clipRect(72, 19, 44, 44);
                g2d.drawImage(imgAvatar, 72, 19, null);
            } catch(IOException e) {
                LOGGER.error("Couldn't get user avatar:", e);
            }
        }
        else {
            g2d.setFont(new Font("Tahoma", Font.BOLD, 16));

            g2d.setColor(Color.LIGHT_GRAY);
            g2d.fillRect(72, 19, 44, 44);

            g2d.setColor(Color.DARK_GRAY);
            g2d.drawString(member.getInitials(), 94 - (g2d.getFontMetrics().stringWidth(member.getInitials()) / 2),
                    47);
        }

        try {
            File tempFile = StreamUtils.tempFileFromImage(image, "test", ".png");
            channel.sendFile(tempFile, null).queue();
        } catch(IOException e) {
            LOGGER.error("Couldn't send test image:", e);
        }
    }

}
