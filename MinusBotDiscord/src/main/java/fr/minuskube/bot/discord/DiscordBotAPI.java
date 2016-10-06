package fr.minuskube.bot.discord;

import fr.minuskube.bot.discord.commands.Command;
import fr.minuskube.bot.discord.games.Game;
import fr.minuskube.bot.discord.listeners.ReadyListener;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.JDABuilder;
import net.dv8tion.jda.entities.User;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.List;

public class DiscordBotAPI {

    private static List<Command> commands = new ArrayList<>();
    private static List<Game> games = new ArrayList<>();

    public static JDA client() { return DiscordBot.instance().getClient(); }
    public static User self() { return client().getSelfInfo(); }
    public static void stop() { DiscordBot.instance().stop(); }

    public static JDA login(String token) throws LoginException, InterruptedException {
        return new JDABuilder().setBotToken(token)
                .addListener(new ReadyListener()).buildBlocking();
    }

    public static void logout() {
        client().shutdown();
    }


    public static void registerCommand(Command cmd) { commands.add(cmd); }

    public static Command getCommand(String name) {
        for(Command cmd : commands)
            if(cmd.getName().equalsIgnoreCase(name) || cmd.getLabels().contains(name.toLowerCase()))
                return cmd;

        return null;
    }

    public static List<Command> getCommands() { return commands; }


    public static void registerGame(Game game) { games.add(game); }

    public static Game getGame(String name) {
        for(Game game : games)
            if(game.getName().equalsIgnoreCase(name))
                return game;

        return null;
    }

    public static Game getGameByUser(User user) {
        for(Game game : games)
            if(game.isPlayer(user))
                return game;

        return null;
    }

    public static List<Game> getGames() { return games; }

}
