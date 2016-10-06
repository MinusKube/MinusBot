package fr.minuskube.bot.discord;

import fr.minuskube.bot.discord.commands.AddCommand;
import fr.minuskube.bot.discord.commands.DrawCommand;
import fr.minuskube.bot.discord.commands.FakeQuoteCommand;
import fr.minuskube.bot.discord.commands.GamesCommand;
import fr.minuskube.bot.discord.commands.GifCommand;
import fr.minuskube.bot.discord.commands.HelpCommand;
import fr.minuskube.bot.discord.commands.InfosCommand;
import fr.minuskube.bot.discord.commands.QuoteCommand;
import fr.minuskube.bot.discord.commands.SexCommand;
import fr.minuskube.bot.discord.commands.StopCommand;
import fr.minuskube.bot.discord.commands.SuggestCommand;
import fr.minuskube.bot.discord.commands.TestCommand;
import fr.minuskube.bot.discord.games.ConnectFourGame;
import fr.minuskube.bot.discord.games.NumberGame;
import fr.minuskube.bot.discord.games.RPSGame;
import fr.minuskube.bot.discord.games.TicTacToeGame;
import fr.minuskube.bot.discord.listeners.CommandListener;
import fr.minuskube.bot.discord.listeners.GameListener;
import net.dv8tion.jda.JDA;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.SelfInfo;
import net.dv8tion.jda.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.time.LocalDateTime;

public class DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);

    public static final Message UNKNOWN_COMMAND = new MessageBuilder()
            .appendString("Sorry, this command is")
            .appendString("unknown", MessageBuilder.Formatting.BOLD)
            .appendString(". Use $help to see available commands.").build();

    private static DiscordBot instance;

    private JDA client;
    private LocalDateTime launchTime;

    public void ready(JDA client) {
        this.client = client;

        LOGGER.info("Registering commands...");
        DiscordBotAPI.registerCommand(new HelpCommand());
        DiscordBotAPI.registerCommand(new InfosCommand());
        DiscordBotAPI.registerCommand(new AddCommand());
        DiscordBotAPI.registerCommand(new SuggestCommand());
        DiscordBotAPI.registerCommand(new GifCommand());
        DiscordBotAPI.registerCommand(new QuoteCommand());
        DiscordBotAPI.registerCommand(new FakeQuoteCommand());
        DiscordBotAPI.registerCommand(new GamesCommand());
        DiscordBotAPI.registerCommand(new TestCommand());
        DiscordBotAPI.registerCommand(new StopCommand());
        DiscordBotAPI.registerCommand(new SexCommand());
        DiscordBotAPI.registerCommand(new DrawCommand());

        LOGGER.info("Registering games...");
        DiscordBotAPI.registerGame(new NumberGame());
        DiscordBotAPI.registerGame(new TicTacToeGame());
        DiscordBotAPI.registerGame(new RPSGame());
        DiscordBotAPI.registerGame(new ConnectFourGame());

        LOGGER.info("Registering listeners...");
        client.addEventListener(new CommandListener(this));
        client.addEventListener(new GameListener(this));

        launchTime = LocalDateTime.now();
        LOGGER.info("MinusBot (Discord) is ready!");
    }

    public void stop() {
        DiscordBotAPI.logout();
        client = null;
    }

    public JDA getClient() { return client; }
    public SelfInfo getSelf() { return client.getSelfInfo(); }
    public User getOwner() { return client.getUserById("87941393766420480"); }
    public LocalDateTime getLaunchTime() { return launchTime; }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            instance = new DiscordBot();

            DiscordBotAPI.login("MTgyMjM3MjIwMjk4Njg2NDY0.Ch0ZIw.V4wbyfW5wNFvMXD0nFaGSgRYbks");
        } catch(LoginException | InterruptedException e) {
            LOGGER.error("Error while login: ", e);
        }
    }

    public static DiscordBot instance() { return instance; }
}
