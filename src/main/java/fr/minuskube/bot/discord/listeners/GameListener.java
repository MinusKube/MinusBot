package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.games.Game;
import fr.minuskube.bot.discord.games.Player;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;

import java.util.List;

public class GameListener extends Listener {

    public GameListener(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Message msg = e.getMessage();

        if(msg.getChannelType() != ChannelType.TEXT)
            return;
        if(msg.getContentDisplay().startsWith(DiscordBotAPI.prefix()))
            return;

        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member member = guild.retrieveMember(msg.getAuthor()).complete();

        Game game = DiscordBotAPI.getGameByUser(member);

        if(game == null)
            return;

        List<Player> players = Player.getPlayers(member);

        if(players.isEmpty())
            return;
        if(game.getData(players.get(0)) == null)
            return;
        if(game.getData(players.get(0)).getChannel() != msg.getChannel())
            return;

        game.receiveMsg(msg);
    }

}
