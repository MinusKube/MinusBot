package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.Messages;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuteCommand extends Command {

    private static Map<Member, List<Guild>> muted = new HashMap<>();

    public MuteCommand() {
        super("mute", "Mutes an user.");

        this.hidden = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(msg.getAuthor() != DiscordBot.instance().getOwner()) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("You don't have the permission to execute this command...",
                            MessageBuilder.Formatting.ITALICS).build())
                    .queue();
            return;
        }

        if(args.length != 1) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                    .appendString("/mute <user>").build())
                    .queue();
            return;
        }

        if(msg.getChannelType() != ChannelType.TEXT) {
            msg.getChannel().sendMessage("You can't mute people in private channels...").queue();
            return;
        }

        TextChannel channel = (TextChannel) msg.getChannel();
        List<Member> mentions = Messages.getMemberMentions(channel.getGuild(), args[0]);

        if(mentions.size() == 1) {
            Member member = mentions.get(0);

            if(!muted.containsKey(member) || !muted.get(member).contains(channel.getGuild())) {
                muted.putIfAbsent(member, new ArrayList<>());
                muted.get(member).add(channel.getGuild());

                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString(member.getEffectiveName() + " has been muted.").build())
                        .queue();
            }
            else {
                muted.get(member).remove(channel.getGuild());

                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString(member.getEffectiveName() + " has been unmuted.").build())
                        .queue();
            }
        }
        else {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                    .appendString("/mute <user>").build())
                    .queue();
        }
    }

    public static boolean isMuted(User user, Guild guild) { return muted.containsKey(user)
            && muted.get(user).contains(guild); }

}
