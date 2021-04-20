package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

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
        TextChannel channel = (TextChannel) msg.getChannel();
        Member author = channel.getGuild().retrieveMember(msg.getAuthor()).complete();

        if(!author.hasPermission(channel, Permission.ADMINISTRATOR)) {
            MessageUtils.error(channel, "*You don't have the permission to execute this command...*").queue();
            return;
        }

        List<Member> mentions = MessageUtils.getMemberMentions(channel.getGuild(), args[0]);

        if(mentions.size() == 1) {
            Member member = mentions.get(0);

            if(!muted.containsKey(member) || !muted.get(member).contains(channel.getGuild())) {
                muted.putIfAbsent(member, new ArrayList<>());
                muted.get(member).add(channel.getGuild());

                channel.sendMessage(new MessageBuilder()
                        .append(member.getEffectiveName() + " has been muted.").build())
                        .queue();
            }
            else {
                muted.get(member).remove(channel.getGuild());

                channel.sendMessage(new MessageBuilder()
                        .append(member.getEffectiveName() + " has been unmuted.").build())
                        .queue();
            }
        }
        else {
            MessageUtils.error(channel, "User not found!").queue();
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) {
        return args.length >= 1;
    }

    public static boolean isMuted(Member member, Guild guild) { return muted.containsKey(member)
            && muted.get(member).contains(guild); }

}
