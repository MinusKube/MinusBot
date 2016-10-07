package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.User;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class Users {

    public static User search(Guild guild, String name) {
        if(!guild.getUsersByName(name).isEmpty())
            return guild.getUsersByName(name).get(0);

        User bestUser = null;
        double bestScore = -1;

        for(User user : guild.getUsers()) {
            double score = StringUtils.getJaroWinklerDistance(user.getUsername(), name);

            if(score <= 0.75)
                continue;

            String nick = guild.getNicknameForUser(user);

            if(nick != null) {
                double nickScore = StringUtils.getJaroWinklerDistance(nick, name);

                if(nickScore > score)
                    score = nickScore;
            }

            if(BigDecimal.valueOf(bestScore).intValue() == -1 || score > bestScore) {
                bestUser = user;
                bestScore = score;
            }
        }

        return bestUser;
    }

    public static User search(Guild guild, String name, String discriminator) {
        for(User user : guild.getUsersByName(name)) {
            if(user.getDiscriminator().equals(discriminator))
                return user;
        }

        User bestUser = null;
        double bestScore = -1;

        for(User user : guild.getUsers()) {
            if(!user.getDiscriminator().equals(discriminator))
                continue;

            double score = StringUtils.getJaroWinklerDistance(user.getUsername(), name);
            String nick = guild.getNicknameForUser(user);

            if(nick != null) {
                double nickScore = StringUtils.getJaroWinklerDistance(nick, name);

                if(nickScore > score)
                    score = nickScore;
            }

            if(BigDecimal.valueOf(bestScore).intValue() == -1 || score > bestScore) {
                bestUser = user;
                bestScore = score;
            }
        }

        return bestUser;
    }

}
