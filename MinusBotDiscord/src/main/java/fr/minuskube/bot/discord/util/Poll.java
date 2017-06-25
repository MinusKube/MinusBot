package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Arc2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Poll {

    private static final Logger LOGGER = LoggerFactory.getLogger(Poll.class);

    private static Map<TextChannel, Poll> polls = new HashMap<>();

    private Member creator;
    private TextChannel channel;

    private String title;
    private String[] choices;
    private Map<Member, Integer> votes = new HashMap<>();

    private Message lastMsg;

    private final int id = new Random().nextInt(100000);
    private int step = 0;

    private boolean ended = false;

    public Poll(Member creator, TextChannel channel, String title, String[] choices) {
        this.creator = creator;
        this.channel = channel;
        this.title = title;
        this.choices = choices;
    }

    public void start() {
        polls.put(channel, this);

        send();
    }

    public boolean hasVoted(Member member) { return votes.containsKey(member); }

    public void vote(Member member, int value) {
        if(hasVoted(member))
            return;
        if(value >= choices.length)
            return;

        votes.put(member, value);
        send();
    }

    public void stop() {
        ended = true;
        polls.remove(channel);

        send();
    }

    public void send() {
        if(lastMsg != null)
            lastMsg.delete().queue();

        try {
            EmbedBuilder builder = new EmbedBuilder()
                    .setDescription("**" + title + "**")
                    .setColor(Color.WHITE);

            for(int i = 0; i < choices.length; i++) {
                String choice = choices[i];

                builder.addField(choice, "**#" + (i + 1) + "**", true);
            }

            builder.addField(!ended ? "Type `#<number>` in the chat to vote." : "Stop voting! This poll has ended!",
                    "Total votes:   `" + votes.size() + "`", false);

            if(votes.size() > 0) {
                String fileName = "pie_chart_" + id + "_" + step + ".png";
                File file = StreamUtils.fileFromImage(new File("/var/www/html/images/bot/" + fileName), createImage());

                builder.setImage("http://minuskube.fr/images/bot/" + fileName);
            }

            builder.setFooter("Poll created by " + creator.getUser().getName(), "");

            channel.sendMessage(builder.build()).queue(msg -> lastMsg = msg);
        } catch(IOException e) {
            LOGGER.error("Couldn't create image: ", e);
        }

        step++;
    }

    private BufferedImage createImage() {
        BufferedImage image = new BufferedImage(310, 160, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);

        g2d.setColor(new Color(54, 57, 62));
        g2d.fill(new RoundRectangle2D.Float(0, 0, 310, 160, 20, 20));

        final double[] votes = getVotesPercent();
        final int startY = (160 - (30 * votes.length - 10)) / 2;

        double lastAngle = 0;

        Color[] colors = new Color[] {
                new Color(222, 207, 63),
                new Color(93, 165, 218),
                new Color(178, 118, 178),
                new Color(96, 189, 104),
                new Color(241, 88, 84)
        };

        for(int i = 0; i < votes.length; i++) {
            double newAngle = votes[i] * 3.6f;

            g2d.setColor(colors[i]);
            g2d.fill(new Arc2D.Double(5, 5, 150, 150, -lastAngle + 90, -(newAngle + 0.5), Arc2D.PIE));

            int x = 175;
            int y = startY + (30 * i);

            g2d.fillOval(x, y, 20, 20);

            g2d.setColor(new Color(220, 220, 220));
            g2d.setFont(new Font("SegoeUI", Font.PLAIN, 16));
            g2d.drawString("#" + (i + 1) + " - " + Math.round(votes[i]) + "%", x + 30, y + 15);

            lastAngle += newAngle;
        }

        g2d.setColor(new Color(0, 0, 0, 0.1f));
        g2d.setStroke(new BasicStroke(12));
        g2d.drawOval(11, 11, 138, 138);

        return image;
    }

    private int[] getVotes() {
        int[] result = new int[choices.length];

        for(int vote : votes.values())
            result[vote]++;

        return result;
    }

    private double[] getVotesPercent() {
        double[] result = new double[choices.length];
        int[] votes = getVotes();

        for(int i = 0; i < votes.length; i++) {
            int amt = votes[i];

            result[i] = amt * 100 / (double) this.votes.size();
        }

        return result;
    }

    public String[] getChoices() { return choices; }
    public TextChannel getChannel() { return channel; }

    public static Map<TextChannel, Poll> getPolls() { return polls; }

}
