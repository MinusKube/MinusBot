package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.awt.FontMetrics;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;

public class Quote {

    public static final String EMOTE_NEXT = "\u23e9";
    public static final String EMOTE_REMOVE = "\u274e";
    private static final Logger LOGGER = LoggerFactory.getLogger(Quote.class);

    private static List<Quote> listening = new ArrayList<>();

    private Member asker;

    private TextChannel channel;
    private Member author;
    private User user;
    private String msg;
    private Date date;

    private List<Message> msgs;

    private Message message;
    private TimerTask task;

    public Quote(Member asker, TextChannel channel, Member author, String msg, Date date) {
        this.asker = asker;
        this.channel = channel;
        this.author = author;
        this.msg = msg;
        this.date = date;

        if(author != null)
            this.user = author.getUser();
    }

    public Quote(Member asker, TextChannel channel, User user, String msg, Date date) {
        this(asker, channel, (Member) null, msg, date);
        this.user = user;
    }

    public Quote(Member asker, TextChannel channel, List<Message> msgs) {
        Message message = msgs.get(0);

        OffsetDateTime odt = message.getCreationTime();
        Date date = Date.from(odt.toInstant());

        this.asker = asker;
        this.channel = channel;
        this.author = !message.isWebhookMessage() ? channel.getGuild().getMember(message.getAuthor()) : null;
        this.user = !message.isWebhookMessage() ? author.getUser() : message.getAuthor();
        this.msg = message.getRawContent();
        this.date = date;

        msgs.remove(0);
        this.msgs = msgs;
    }

    public void send() {
        channel.sendMessage(buildEmbed()).queue(msg -> {
            this.message = msg;

            if(msgs != null && hasNext())
                msg.addReaction(EMOTE_NEXT).queue();

            msg.addReaction(EMOTE_REMOVE).queue();

            listening.add(this);
            startTask();
        });
    }

    private void startTask() {
        task = new TimerTask() {
            @Override
            public void run() {
                listening.remove(Quote.this);

                try {
                    if(hasNext())
                        MessageUtils.removeReaction(EMOTE_NEXT, message).queue();

                    MessageUtils.removeReaction(EMOTE_REMOVE, message).queue();
                } catch(IllegalArgumentException ignored) {}
            }
        };

        new Timer().schedule(task, 1000 * 20);
    }

    private MessageEmbed buildEmbed() {
        SimpleDateFormat formatter = new SimpleDateFormat("EEE dd/MM 'at' HH:mm");
        formatter.setTimeZone(TimeZone.getTimeZone(ZoneId.of("Europe/Paris")));

        Color color = author != null ? author.getColor() : null;
        String avatar = user.getAvatarId() != null
                ? user.getAvatarUrl() : user.getDefaultAvatarUrl();

        return new EmbedBuilder()
                .setColor(color)
                .setDescription(msg)
                .setFooter((author != null ? author.getEffectiveName() : user.getName())
                        + " | " + formatter.format(date), avatar)
                .build();
    }

    public boolean hasNext() {
        return msgs != null && !msgs.isEmpty();
    }

    public void next() {
        Message message = msgs.remove(0);

        OffsetDateTime odt = message.getCreationTime();
        Date date = Date.from(odt.toInstant());

        this.author = !message.isWebhookMessage() ? channel.getGuild().getMember(message.getAuthor()) : null;
        this.user = !message.isWebhookMessage() ? author.getUser() : message.getAuthor();
        this.msg = message.getRawContent();
        this.date = date;

        Message newMsg = new MessageBuilder().setEmbed(buildEmbed()).build();
        this.message.editMessage(newMsg).queue();

        try {
            if(!hasNext())
                MessageUtils.removeReaction(EMOTE_NEXT, this.message).queue();
        } catch(IllegalArgumentException ignored) {}

        if(task.cancel())
            startTask();
        else
            LOGGER.warn("Can't stop task!");
    }

    public void delete() {
        listening.remove(this);
        message.delete().queue();
    }

    private List<String> splitString(FontMetrics metrics, String txt) {
        int maxWidth = 275;

        List<String> result = new ArrayList<>();

        String[] lines = txt.split("\n");

        for(String line : lines) {
            String[] words = line.split(" ");
            String currentLine = words[0];

            for(int i = 1; i < words.length; i++) {
                if(words[i].contains("\n") || words[i].contains("\r")) {
                    result.add(currentLine);
                    currentLine = words[i];

                    continue;
                }

                if(metrics.stringWidth(currentLine + words[i]) < maxWidth)
                    currentLine += " " + words[i];
                else {
                    result.add(currentLine);
                    currentLine = words[i];
                }
            }

            if(currentLine.trim().length() > 0)
                result.add(currentLine);
        }

        return result;
    }

    public Member getAsker() { return asker; }

    public static Quote fromMessageId(String messageId) {
        for(Quote quote : listening)
            if(quote.message.getId().equals(messageId))
                return quote;

        return null;
    }

}
