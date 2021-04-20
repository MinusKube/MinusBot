package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.Poll;
import fr.minuskube.bot.discord.util.PollCreation;
import fr.minuskube.bot.discord.util.TabbedList;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;

import java.util.Collections;

public class PollCommand extends Command {

    public PollCommand() {
        super("poll", Collections.singletonList("vote"), "Starts a poll with different choices.", "");

        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member member = guild.retrieveMember(msg.getAuthor()).complete();

        if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.delete().queue();

        /*

            poll: Shows list of poll commands
            poll create: Creates a poll (Need to be admin)
                -> 0: Asks the user to enter the title
                -> 1: Asks the user to enter the first choice
                -> 2: Asks the user to enter the second choice
                -> 3: Asks the user to enter other choices or "end" to finish the poll and starts it
                - (All the messages of creation are deleted)
            poll stop: Deletes a poll  (Need to be admin)
            poll show: Sends the current poll

         */

        if(args.length == 0) {
            channel.sendMessage(availableCommands()).queue();
            return;
        }

        switch(args[0].toLowerCase()) {
            case "create": {
                if(!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
                    MessageUtils.error(channel,
                            "*You don't have the permission to execute this command...*").queue();
                    return;
                }
                if(PollCreation.getCreations().containsKey(channel)) {
                    MessageUtils.error(channel, "A poll is already being created.").queue();
                    return;
                }

                new PollCreation(member, channel).start();
                break;
            }

            case "stop": {
                if(!member.hasPermission(channel, Permission.ADMINISTRATOR)) {
                    MessageUtils.error(channel,
                            "*You don't have the permission to execute this command...*").queue();
                    return;
                }
                if(!Poll.getPolls().containsKey(channel)) {
                    MessageUtils.error(channel, "There is no poll on this channel.").queue();
                    return;
                }

                Poll.getPolls().get(channel).stop();
                break;
            }

            case "show": {
                if(!Poll.getPolls().containsKey(channel)) {
                    MessageUtils.error(channel, "There is no poll on this channel.").queue();
                    return;
                }

                Poll.getPolls().get(channel).send();
                break;
            }
        }

        /*
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
        }*/
    }

    private String availableCommands() {
        String p = DiscordBotAPI.prefix();

        return "```" + new TabbedList(new String[] {
                "Name:", "",
                p + "poll",
                p + "poll create",
                p + "poll stop",
                p + "poll show"
        }, new String[] {
                "Description:", "",
                "Displays list of commands",
                "Creates a poll",
                "Deletes a poll",
                "Shows the current poll"
        }, new String[] {
                "Permission:", "",
                "None",
                "Admin",
                "Admin",
                "None"
        }).toString() + "```";
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

}
