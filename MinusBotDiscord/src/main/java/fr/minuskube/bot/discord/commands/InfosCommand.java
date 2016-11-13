package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.Webhook;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class InfosCommand extends Command {

    public InfosCommand() {
        super("infos", Collections.singletonList("info"), "Shows some informations on the bot.");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();

        LocalDateTime time = DiscordBot.instance().getLaunchTime();
        Duration uptime = Duration.between(time, LocalDateTime.now());

        if(!(channel instanceof TextChannel)) {
            channel.sendMessage(new MessageBuilder()
                    .appendString("  \u00bb Author: ", MessageBuilder.Formatting.BOLD)
                    .appendString("MinusKube").appendString("\n")
                    .appendString("  \u00bb Libraries: ", MessageBuilder.Formatting.BOLD)
                    .appendString("JDA, Giphy4J, JTidy").appendString("\n")
                    .appendString("  \u00bb Uptime: ", MessageBuilder.Formatting.BOLD)
                    .appendString(DurationFormatUtils.formatDuration(uptime.toMillis(), "D'd' HH'h' MM'm'")).build())
                    .queue();
        }
        else {
            TextChannel textChannel = (TextChannel) channel;

            JSONObject fieldLibs = new JSONObject(new HashMap<String, Object>() {{
                put("name", "Libraries");
                put("value", "**JDA, Giphy4J, JTidy, **");
            }});

            JSONObject fieldUptime = new JSONObject(new HashMap<String, Object>() {{
                put("name", "Uptime");
                put("value", "**" + DurationFormatUtils.formatDuration(uptime.toMillis(),
                        "d'd' HH'h' MM'm'") + "**");
            }});

            JSONObject fieldGithub = new JSONObject(new HashMap<String, Object>() {{
                put("name", "Github");
                put("value", "**https://github.com/MinusKube/MinusBot**");
            }});

            JSONObject embed = new JSONObject(new HashMap<String, Object>() {{
                put("description", "*Some informations on " + DiscordBotAPI.self().getAsMention() + ".*");
                put("color", 13158450);

                put("fields", new JSONArray(new ArrayList<Object>() {{
                    add(fieldLibs);
                    add(fieldUptime);
                    add(fieldGithub);
                }}));

                put("footer", new HashMap<String, Object>() {{
                    put("text", "by MinusKube");
                    put("icon_url", "http://minuskube.fr/logo_transparent_crop.png");
                }});
            }});

            Webhook webhook = Webhook.getBotHook(textChannel);

            if(webhook == null) {
                channel.sendMessage("Error while creating quote!").queue();
                return;
            }

            webhook.execute(new JSONObject(new HashMap<String, Object>() {{
                put("embeds", new JSONArray(Collections.singleton(embed)));
            }}));
        }

    }

}
