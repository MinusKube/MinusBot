package fr.minuskube.bot.discord.commands;

import at.mukprojects.giphy4j.Giphy;
import at.mukprojects.giphy4j.entity.search.SearchRandom;
import at.mukprojects.giphy4j.exception.GiphyException;
import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.MessageChannel;
import net.dv8tion.jda.entities.TextChannel;

public class GifCommand extends Command {

    private static final String API_KEY = "dc6zaTOxFJmzC";

    public GifCommand() {
        super("gif", "Prints a random gif!");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();
        Giphy giphy = new Giphy(API_KEY);

        try {
            SearchRandom giphySearch = giphy.searchRandom(args.length > 0 ? args[0] : "");

            channel.sendMessage(new MessageBuilder()
                    .appendMention(msg.getAuthor())
                    .appendString(args.length > 0 ? " (" + args[0] + ")\n" : "\n")
                    .appendString(giphySearch.getData().getImageOriginalUrl()).build());
        } catch (GiphyException e) {
            channel.sendMessage(new MessageBuilder()
                    .appendMention(msg.getAuthor())
                    .appendString(args.length > 0 ? " (" + args[0] + ")\n" : "\n")
                    .appendString("No gif found.").build());
        }

        if(!msg.isPrivate() && ((TextChannel) channel).checkPermission(DiscordBotAPI.self(),
                Permission.MESSAGE_MANAGE)) {

            msg.deleteMessage();
        }
    }

}
