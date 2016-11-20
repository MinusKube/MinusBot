package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.Poll;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PollCommand extends Command {

    private Map<TextChannel, Poll> polls = new HashMap<>();

    public PollCommand() {
        super("poll", Collections.singletonList("vote"), "Starts a poll with different choices.", "");

        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();

        if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.deleteMessage().queue();

        if(!polls.containsKey(channel)) {
            channel.sendMessage("This feature is still in development.").queue();
        }
        else {
            MessageUtils.error(channel, "There is already a poll on this channel.").queue();
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }
}
