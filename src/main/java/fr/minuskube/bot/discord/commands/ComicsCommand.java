package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.comics.CommitStrip;
import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.TabbedList;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

import java.util.Collections;

public class ComicsCommand extends Command {

    public ComicsCommand() {
        super("comics", Collections.singletonList("comic"), "Adds/removes notifications for new comics on a channel.",
                "");

        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        final String prefix = DiscordBotAPI.prefix();

        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member author = guild.retrieveMember(msg.getAuthor()).complete();

        if(!author.hasPermission(channel, Permission.ADMINISTRATOR)) {
            MessageUtils.error(channel, "*You don't have the permission to execute this command...*").queue();
            return;
        }

        if(args.length == 0) {
            channel.sendMessage(availableCommands()).queue();
            return;
        }

        CommitStrip commitStrip = DiscordBot.instance().getCommitStrip();

        switch(args[0].toLowerCase()) {
            case "on": {
                if(commitStrip.getChannels().contains(channel)) {
                    MessageUtils.error(channel, "This channel is already allowed.").queue();
                    return;
                }

                commitStrip.getChannels().add(channel);
                commitStrip.save();

                channel.sendMessage("This channel can now receive comics notifications!").queue();
                break;
            }
            case "off": {
                if(!commitStrip.getChannels().contains(channel)) {
                    MessageUtils.error(channel, "This channel isn't allowed.").queue();
                    return;
                }

                commitStrip.getChannels().remove(channel);
                commitStrip.save();

                channel.sendMessage("This channel can't receive comics notifications anymore.").queue();
                break;
            }
            case "list": {
                channel.sendMessage(list()).queue();
                break;
            }

            default: {
                MessageUtils.error(channel, "Unknown command, type " + prefix + "comics to see the available commands.")
                        .queue();
                break;
            }
        }
    }

    private String list() {
        String p = DiscordBotAPI.prefix();

        String list = new TabbedList(new String[] {
                "- CommitStrip",
        }, new String[] {
                "http://www.commitstrip.com/",
        }).toString();

        return "**Comics:**\n" +
                list + "\n\n" +
                "Use " + p + "suggest to propose other websites (They need to have a RSS Feed).";
    }

    private String availableCommands() {
        String p = DiscordBotAPI.prefix();

        String list = new TabbedList(new String[] {
                p + "comics on <language>",
                p + "comics off",
                p + "comics list"
        }, new String[] {
                "Allows this channel to receive new comics notifications.",
                "Disallows this channel from receiving new comics notifications.",
                "Displays list of websites that are checked."
        }).toString();

        return "Available commands:\n" +
                "```" +
                list + "\n" +
                "```";
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

    private void wrongSyntax(MessageChannel channel, String syntax) {
        MessageUtils.error(channel, "Wrong Syntax",
                DiscordBotAPI.prefix() + "comics " + syntax).queue();
    }

}
