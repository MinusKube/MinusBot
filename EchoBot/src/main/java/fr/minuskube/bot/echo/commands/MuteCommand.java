package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuteCommand extends Command {

    private static Map<Member, List<Guild>> muted = new HashMap<>();

    public MuteCommand() {
        super("mute", "Mutes an user.", "<user>");

        this.hidden = true;
        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(msg.getAuthor() != DiscordBot.instance().getOwner()) {
            MessageUtils.error(msg.getChannel(), "*You don't have the permission to execute this command...*").queue();
            return;
        }

        TextChannel channel = (TextChannel) msg.getChannel();
        List<Member> mentions = MessageUtils.getMemberMentions(channel.getGuild(), args[0]);

        if(mentions.size() == 1) {
            Member member = mentions.get(0);

            if(!muted.containsKey(member) || !muted.get(member).contains(channel.getGuild())) {
                muted.putIfAbsent(member, new ArrayList<>());
                muted.get(member).add(channel.getGuild());

                channel.sendMessage(new MessageBuilder()
                        .appendString(member.getEffectiveName() + " has been muted.").build())
                        .queue();
            }
            else {
                muted.get(member).remove(channel.getGuild());

                channel.sendMessage(new MessageBuilder()
                        .appendString(member.getEffectiveName() + " has been unmuted.").build())
                        .queue();
            }
        }
        else {
            MessageUtils.error(channel, "User not found!");
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) {
        return args.length >= 1;
    }

    public static boolean isMuted(Member member, Guild guild) { return muted.containsKey(member)
            && muted.get(member).contains(guild); }

}
