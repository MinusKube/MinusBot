package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.games.Game;
import fr.minuskube.bot.discord.games.Player;
import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Arrays;

public class GamesCommand extends Command {

    public GamesCommand() {
        super("games", Arrays.asList("game", "g"), "Play a game with me, or with a friend!",
                "");

        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();

        if(args.length < 1) {
            MessageBuilder builder = new MessageBuilder()
                    .append("List of available games:", MessageBuilder.Formatting.BOLD);

            for(Game game : DiscordBotAPI.getGames())
                builder.append("\n  - `" + game.getName() + "` : ")
                        .append(game.getDescription(), MessageBuilder.Formatting.ITALICS);

            builder.append("\n\nUse `" + DiscordBotAPI.prefix() + "games <name>` to start a game.")
                    .append("\nUse `" + DiscordBotAPI.prefix() + "game leave` to leave a game.");

            channel.sendMessage(builder.build()).queue();
            return;
        }

        if(args[0].equalsIgnoreCase("leave")) {
            Game game = DiscordBotAPI.getGameByUser(guild.getMember(msg.getAuthor()));

            if(game == null) {
                MessageUtils.error(channel, "You're not in a game.").queue();
                return;
            }

            Player p = Player.getPlayers(guild.getMember(msg.getAuthor())).get(0);
            game.end(p, channel);

            channel.sendMessage(new MessageBuilder()
                    .append("Game successfully left.").build())
                    .queue();
            return;
        }

        Game curGame = DiscordBotAPI.getGameByUser(guild.getMember(msg.getAuthor()));

        if(curGame != null && (!curGame.doesAllowDuplicate() || curGame.isFull())) {
            MessageUtils.error(channel, "You're already in a game. Type \""
                    + DiscordBotAPI.prefix() + "game leave\" to leave it.").queue();
            return;
        }

        Game game = DiscordBotAPI.getGame(args[0]);

        if(game == null) {
            MessageUtils.error(channel, "This game doesn't exist.").queue();
            return;
        }

        game.start(new Player(guild.getMember(msg.getAuthor())), channel);
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }
}
