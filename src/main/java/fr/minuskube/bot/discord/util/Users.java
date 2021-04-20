package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import org.apache.commons.lang3.StringUtils;

import java.math.BigDecimal;

public class Users {

    public static Member search(Guild guild, String name) {
        if(!guild.getMembersByName(name, true).isEmpty())
            return guild.getMembersByName(name, true).get(0);
        if(!guild.getMembersByNickname(name, true).isEmpty())
            return guild.getMembersByNickname(name, true).get(0);

        Member bestMember = null;
        double bestScore = -1;

        for(Member member : guild.getMembers()) {
            double score = StringUtils.getJaroWinklerDistance(member.getUser().getName(), name);

            if(score <= 0.65)
                continue;

            String nick = member.getNickname();

            if(nick != null) {
                double nickScore = StringUtils.getJaroWinklerDistance(nick, name);

                if(nickScore > score)
                    score = nickScore;
            }

            if(BigDecimal.valueOf(bestScore).intValue() == -1 || score > bestScore) {
                bestMember = member;
                bestScore = score;
            }
        }

        return bestMember;
    }

    public static Member search(Guild guild, String name, String discriminator) {
        for(Member member : guild.getMembersByName(name, true)) {
            if(member.getUser().getDiscriminator().equals(discriminator))
                return member;
        }

        Member bestMember = null;
        double bestScore = -1;

        for(Member member : guild.getMembers()) {
            if(!member.getUser().getDiscriminator().equals(discriminator))
                continue;

            double score = StringUtils.getJaroWinklerDistance(member.getUser().getName(), name);
            String nick = member.getNickname();

            if(nick != null) {
                double nickScore = StringUtils.getJaroWinklerDistance(nick, name);

                if(nickScore > score)
                    score = nickScore;
            }

            if(BigDecimal.valueOf(bestScore).intValue() == -1 || score > bestScore) {
                bestMember = member;
                bestScore = score;
            }
        }

        return bestMember;
    }

}
