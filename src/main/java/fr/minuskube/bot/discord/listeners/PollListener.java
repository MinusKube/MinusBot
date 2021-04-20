package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.Poll;
import fr.minuskube.bot.discord.util.PollCreation;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

public class PollListener extends Listener {

    public PollListener(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Message msg = e.getMessage();

        if(msg.getChannelType() != ChannelType.TEXT)
            return;

        checkVote(msg);
        checkCreation(msg);
    }

    private void checkVote(Message msg) {
        if(msg.getContentDisplay().length() != 2)
            return;
        if(!msg.getContentDisplay().startsWith("#"))
            return;

        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member member = guild.retrieveMember(msg.getAuthor()).complete();

        Poll poll = Poll.getPolls().get(channel);

        if(poll == null)
            return;

        try {
            if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                msg.delete().queue();

            int input = Integer.parseInt(msg.getContentDisplay().substring(1));

            if(poll.hasVoted(member))
                MessageUtils.error(channel, "You already voted on this poll!").queue();
            else if(input < 1 || input > poll.getChoices().length)
                MessageUtils.error(channel, "Invalid number.").queue();
            else
                poll.vote(member, input - 1);
        } catch(NumberFormatException ignored) {}
    }

    private void checkCreation(Message msg) {
        if(msg.getContentDisplay().startsWith("$"))
            return;

        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member member = guild.retrieveMember(msg.getAuthor()).complete();

        PollCreation creation = PollCreation.getCreations().get(channel);

        if(creation == null)
            return;
        if(creation.getMember() != member)
            return;

        creation.receive(msg);
    }

}
