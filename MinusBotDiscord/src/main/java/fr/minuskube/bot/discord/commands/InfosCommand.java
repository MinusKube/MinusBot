package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

public class InfosCommand extends Command {

    public InfosCommand() {
        super("infos", Collections.singletonList("info"), "Shows some informations on the bot.");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();

        LocalDateTime time = DiscordBot.instance().getLaunchTime();
        Duration uptime = Duration.between(time, LocalDateTime.now());

        channel.sendMessage(new MessageBuilder()
                .appendString("  \u00bb Author: ", MessageBuilder.Formatting.BOLD)
                .appendString("MinusKube").appendString("\n")
                .appendString("  \u00bb Libraries: ", MessageBuilder.Formatting.BOLD)
                .appendString("JDA, Giphy4J, JTidy").appendString("\n")
                .appendString("  \u00bb Uptime: ", MessageBuilder.Formatting.BOLD)
                .appendString(DurationFormatUtils.formatDuration(uptime.toMillis(), "HH:mm:ss")).build());
    }

}
