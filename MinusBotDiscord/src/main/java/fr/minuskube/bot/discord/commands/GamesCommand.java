package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.games.Game;
import fr.minuskube.bot.discord.games.Player;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;

public class GamesCommand extends Command {

    public GamesCommand() {
        super("games", Arrays.asList("game", "g"), "Play a game with me!");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(!(msg.getChannel() instanceof TextChannel)) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("You can't start games in private channels.").build())
                    .queue();
            return;
        }

        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();

        if(args.length < 1) {
            MessageBuilder builder = new MessageBuilder()
                    .appendString("List of available games:", MessageBuilder.Formatting.BOLD);

            for(Game game : DiscordBotAPI.getGames())
                builder.appendString("\n  - `" + game.getName() + "` : ")
                        .appendString(game.getDescription(), MessageBuilder.Formatting.ITALICS);

            builder.appendString("\n\nUse `$games <name>` to start a game.")
                    .appendString("\nUse `$game leave` to leave a game.");

            channel.sendMessage(builder.build()).queue();
            return;
        }

        if(args[0].equalsIgnoreCase("leave")) {
            Game game = DiscordBotAPI.getGameByUser(guild.getMember(msg.getAuthor()));

            if(game == null) {
                channel.sendMessage(new MessageBuilder()
                        .appendString("You're not in a game.").build())
                        .queue();
                return;
            }

            Player p = Player.getPlayers(guild.getMember(msg.getAuthor())).get(0);
            game.end(p, channel);

            channel.sendMessage(new MessageBuilder()
                    .appendString("Game successfully left.").build())
                    .queue();
            return;
        }

        Game curGame = DiscordBotAPI.getGameByUser(guild.getMember(msg.getAuthor()));

        if(curGame != null && (!curGame.doesAllowDuplicate() || curGame.isFull())) {
            channel.sendMessage(new MessageBuilder()
                    .appendString("You're already in a game. Type \"$game leave\" to leave it.").build())
                    .queue();
            return;
        }

        Game game = DiscordBotAPI.getGame(args[0]);

        if(game == null) {
            channel.sendMessage(new MessageBuilder()
                    .appendString("This game doesn't exist.").build())
                    .queue();
            return;
        }

        game.start(new Player(guild.getMember(msg.getAuthor())), channel);
    }

}
