package fr.minuskube.bot.discord;

import fr.minuskube.bot.discord.commands.Command;
import fr.minuskube.bot.discord.games.Game;
import fr.minuskube.bot.discord.listeners.ReadyListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DiscordBotAPI {

    private static List<Command> commands = new ArrayList<>();
    private static List<Game> games = new ArrayList<>();

    public static JDA client() { return DiscordBot.instance().getClient(); }
    public static SelfUser self() { return client().getSelfUser(); }
    public static void stop() { DiscordBot.instance().stop(); }

    public static JDA login(String token) throws LoginException, InterruptedException, RateLimitedException {
        return new JDABuilder(AccountType.BOT).setToken(token)
                .addListener(new ReadyListener()).buildBlocking();
    }

    public static void logout() {
        client().shutdown();
    }


    public static void registerCommands(Command... cmds) {
        Collections.addAll(commands, cmds);
    }

    public static Command getCommand(String name) {
        for(Command cmd : commands)
            if(cmd.getName().equalsIgnoreCase(name) || cmd.getLabels().contains(name.toLowerCase()))
                return cmd;

        return null;
    }

    public static List<Command> getCommands() { return commands; }


    public static void registerGames(Game... gs) {
        Collections.addAll(games, gs);
    }

    public static Game getGame(String name) {
        for(Game game : games)
            if(game.getName().equalsIgnoreCase(name))
                return game;

        return null;
    }

    public static Game getGameByUser(Member member) {
        for(Game game : games)
            if(game.isPlayer(member))
                return game;

        return null;
    }

    public static List<Game> getGames() { return games; }

}
