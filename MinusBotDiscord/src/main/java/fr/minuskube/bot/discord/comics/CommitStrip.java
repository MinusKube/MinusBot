package fr.minuskube.bot.discord.comics;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class CommitStrip extends TimerTask {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommitStrip.class);
    private static final String FEED_URL = "http://www.commitstrip.com/fr/feed/";

    private List<TextChannel> channels = new ArrayList<>();
    private String lastTitle;

    public void load() {
        try {
            Path path = Paths.get("comics.txt");

            if(!Files.exists(path))
                return;

            for(String line : Files.readAllLines(path)) {
                TextChannel channel = DiscordBotAPI.client().getTextChannelById(line);

                if(channel != null)
                    channels.add(channel);
            }
        } catch(IOException e) {
            LOGGER.error("Error while loading channels: ", e);
        }
    }

    public void save() {
        try {
            Path path = Paths.get("comics.txt");

            StringBuilder sb = new StringBuilder();

            for(TextChannel channel : channels) {
                if(sb.length() > 0)
                    sb.append("\n");

                sb.append(channel.getId());
            }

            Files.write(path, sb.toString().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch(IOException e) {
            LOGGER.error("Error while saving channels: ", e);
        }
    }

    public void start() { new Timer().scheduleAtFixedRate(this, 1000, 60 * 1000); }

    @Override
    public void run() {
        try {
            URL url = new URL(FEED_URL);

            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));

            SyndEntry entry = feed.getEntries().get(0);
            String title = entry.getTitle();
            String link = entry.getLink();

            if(this.lastTitle == null)
                this.lastTitle = title;

            if(!this.lastTitle.equalsIgnoreCase(title)) {
                send(link);

                this.lastTitle = title;
            }
        } catch(FeedException | IOException e) {
            LOGGER.error("Can't retrieve feed: ", e);
        }
    }

    private void send(String link) {
        for(TextChannel channel : channels) {
            channel.sendMessage(new MessageBuilder()
                    .append("A new CommitStrip is out!\n", MessageBuilder.Formatting.BOLD)
                    .append(link).build()).queue();
        }
    }

    public List<TextChannel> getChannels() { return channels; }

}
