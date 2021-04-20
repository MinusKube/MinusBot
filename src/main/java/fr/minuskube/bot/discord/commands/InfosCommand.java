package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.apache.commons.lang3.time.DurationFormatUtils;

import java.awt.*;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Collections;

public class InfosCommand extends Command {

    public InfosCommand() {
        super("infos", Collections.singletonList("info"), "Shows some information about the bot.", "");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();

        LocalDateTime time = DiscordBot.instance().getLaunchTime();
        Duration uptime = Duration.between(time, LocalDateTime.now());

        if(!(channel instanceof TextChannel)) {
            channel.sendMessage(new MessageBuilder()
                    .append("  \u00bb Author: ", MessageBuilder.Formatting.BOLD)
                    .append("MinusKube").append("\n")
                    .append("  \u00bb Libraries: ", MessageBuilder.Formatting.BOLD)
                    .append("JDA, Giphy4J, JTidy").append("\n")
                    .append("  \u00bb Uptime: ", MessageBuilder.Formatting.BOLD)
                    .append(DurationFormatUtils.formatDuration(uptime.toMillis(), "d'd' HH'h' mm'm'")).append("\n")
                    .append("  \u00bb Github: ", MessageBuilder.Formatting.BOLD)
                    .append("https://github.com/MinusKube/MinusBot").append("\n").build())
                    .queue();
        }
        else {
            TextChannel textChannel = (TextChannel) channel;


            if(textChannel.getGuild().getSelfMember().hasPermission(textChannel, Permission.MESSAGE_MANAGE))
                msg.delete().queue();

            MessageEmbed embed = new EmbedBuilder()
                    .addField("Libraries", "JDA, Giphy4J, JTidy", false)
                    .addField("Uptime", DurationFormatUtils.formatDuration(uptime.toMillis(),
                            "d'd' HH'h' mm'm'"), false)
                    .addField("Github", "https://github.com/MinusKube/MinusBot", false)
                    .setDescription("*Some information about " + DiscordBotAPI.self().getAsMention() + ".*")
                    .setColor(Color.YELLOW)
                    .setFooter("by MinusKube", "http://minuskube.fr/logo_transparent_crop.png")
                    .build();

            textChannel.sendMessage(embed).queue();
        }

    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

}
