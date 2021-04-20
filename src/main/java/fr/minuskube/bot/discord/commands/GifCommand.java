package fr.minuskube.bot.discord.commands;

import at.mukprojects.giphy4j.Giphy;
import at.mukprojects.giphy4j.entity.search.SearchRandom;
import at.mukprojects.giphy4j.exception.GiphyException;
import fr.minuskube.bot.discord.Config;
import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.*;

public class GifCommand extends Command {

    public GifCommand() {
        super("gif", "Prints a random gif!", "");
    }

    @Override
    public void execute(Message msg, String[] args) {
        Config config = DiscordBot.instance().getConfig();

        MessageChannel channel = msg.getChannel();
        Giphy giphy = new Giphy(config.getGiphyApiKey());

        try {
            SearchRandom giphySearch = giphy.searchRandom(args.length > 0 ? args[0] : "");

            channel.sendMessage(new MessageBuilder()
                    .append(msg.getAuthor())
                    .append(args.length > 0 ? " (" + args[0] + ")\n" : "\n")
                    .append(giphySearch.getData().getImageOriginalUrl()).build())
                    .queue();
        } catch (GiphyException e) {
            MessageUtils.error(channel, "No gif found.").queue();
        }

        if(channel.getType() == ChannelType.TEXT) {
            TextChannel tc = (TextChannel) channel;
            Guild guild = tc.getGuild();

            if(guild.getSelfMember().hasPermission(tc, Permission.MESSAGE_MANAGE))
                msg.delete().queue();
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

}
