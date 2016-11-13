package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.Poll;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PollCommand extends Command {

    private Map<TextChannel, Poll> polls = new HashMap<>();

    public PollCommand() {
        super("poll", Collections.singletonList("vote"), "Starts a poll with different choices.");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(msg.getChannelType() != ChannelType.TEXT) {
            msg.getChannel().sendMessage("You can't make polls in private channels.").queue();
            return;
        }

        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();

        if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.deleteMessage().queue();

        if(!polls.containsKey(channel)) {

        }
        else {
            channel.sendMessage("There is already a poll on this channel.").queue();
        }
    }

}
