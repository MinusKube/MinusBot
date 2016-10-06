package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Role;
import net.dv8tion.jda.entities.User;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Messages {

    public static String replaceMentions(Guild guild, String msg) {
        Pattern pattern = Pattern.compile("<(@|@!|@&|#)(\\d+)>");
        Matcher matcher = pattern.matcher(msg);

        StringBuffer result = new StringBuffer();

        while(matcher.find()) {
            boolean channelMention = matcher.group(1).equals("#");
            String id = matcher.group(2);

            if(!channelMention) {
                User user = guild.getUserById(id);

                if(user != null) {
                    String name = guild.getEffectiveNameForUser(user);
                    matcher.appendReplacement(result, "@" + name);

                    continue;
                }

                Role role = guild.getRoleById(id);

                if(role != null)
                    matcher.appendReplacement(result, "@" + role.getName());
            }
            else {
                Channel channel = guild.getJDA().getTextChannelById(id);

                if(channel != null)
                    matcher.appendReplacement(result, "#" + channel.getName());
            }
        }

        matcher.appendTail(result);
        return result.toString();
    }

}
