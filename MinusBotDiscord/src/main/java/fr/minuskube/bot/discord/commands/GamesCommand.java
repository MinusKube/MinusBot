package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.games.Game;
import fr.minuskube.bot.discord.games.Player;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;

import java.util.Arrays;

public class GamesCommand extends Command {

    public GamesCommand() {
        super("games", Arrays.asList("game", "g"), "Play a game with me!");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(!(msg.getChannel() instanceof TextChannel)) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("You can't start games in private channels.").build());
            return;
        }

        if(args.length < 1) {
            MessageBuilder builder = new MessageBuilder()
                    .appendString("List of available games:", MessageBuilder.Formatting.BOLD);

            for(Game game : DiscordBotAPI.getGames())
                builder.appendString("\n  - `" + game.getName() + "` : ")
                        .appendString(game.getDescription(), MessageBuilder.Formatting.ITALICS);

            builder.appendString("\n\nUse `$games <name>` to start a game.")
                    .appendString("\nUse `$game leave` to leave a game.");

            msg.getChannel().sendMessage(builder.build());
            return;
        }

        if(args[0].equalsIgnoreCase("leave")) {
            Game game = DiscordBotAPI.getGameByUser(msg.getAuthor());

            if(game == null) {
                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString("You're not in a game.").build());
                return;
            }

            Player p = Player.getPlayers(msg.getAuthor()).get(0);
            game.end(p, (TextChannel) msg.getChannel());

            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Game successfully left.").build());
            return;
        }

        Game curGame = DiscordBotAPI.getGameByUser(msg.getAuthor());

        if(curGame != null && (!curGame.doesAllowDuplicate() || curGame.isFull())) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("You're already in a game. Type \"$game leave\" to leave it.").build());
            return;
        }

        Game game = DiscordBotAPI.getGame(args[0]);

        if(game == null) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("This game doesn't exist.").build());
            return;
        }

        game.start(new Player(msg.getAuthor()), (TextChannel) msg.getChannel());
    }

}
