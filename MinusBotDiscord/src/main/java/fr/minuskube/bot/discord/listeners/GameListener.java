package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.games.Game;
import fr.minuskube.bot.discord.games.Player;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.message.MessageReceivedEvent;

import java.util.List;

public class GameListener extends Listener {

    public GameListener(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Message msg = e.getMessage();

        if(msg.getContent().startsWith("$"))
            return;

        Game game = DiscordBotAPI.getGameByUser(msg.getAuthor());

        if(game == null)
            return;

        List<Player> players = Player.getPlayers(msg.getAuthor());

        if(players.isEmpty())
            return;
        if(game.getData(players.get(0)) == null)
            return;
        if(game.getData(players.get(0)).getChannel() != msg.getChannel())
            return;

        game.receiveMsg(msg);
    }

}
