package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.Poll;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PollCommand extends Command {

    private static Map<TextChannel, Poll> polls = new HashMap<>();

    public PollCommand() {
        super("poll", Collections.singletonList("vote"), "Starts a poll with different choices.", "");

        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member member = guild.getMember(msg.getAuthor());

        if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.deleteMessage().queue();

        /*

            poll: Shows list of poll commands
            poll create: Creates a poll (Need to be admin)
                -> Asks the user to enter the title
                -> Asks the user to enter the first item
                -> Asks the user to enter the second item
                -> Asks the user to enter other items or "end" to finish the poll and starts it
                - (All the messages of creation are deleted)
            poll stop: Deletes a poll  (Need to be admin)
            poll show: Sends the current poll

         */

        if(!polls.containsKey(channel)) {
            Poll poll = new Poll(member, channel, "Why are you a potato?", new String[] {
                    "Because I love potatoes",
                    "Because potatoes are life",
                    "Because I am a potato"
            });

            poll.send();
            polls.put(channel, poll);
        }
        else {
            MessageUtils.error(channel, "There is already a poll on this channel.").queue();
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

    public static Map<TextChannel, Poll> getPolls() { return polls; }
    public static Poll getPoll(TextChannel channel) { return polls.get(channel); }

}
