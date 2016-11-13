package fr.minuskube.bot.discord;

import com.google.gson.Gson;
import fr.minuskube.bot.discord.commands.AddCommand;
import fr.minuskube.bot.discord.commands.DrawCommand;
import fr.minuskube.bot.discord.commands.FakeQuoteCommand;
import fr.minuskube.bot.discord.commands.GamesCommand;
import fr.minuskube.bot.discord.commands.GifCommand;
import fr.minuskube.bot.discord.commands.HelpCommand;
import fr.minuskube.bot.discord.commands.InfosCommand;
import fr.minuskube.bot.discord.commands.MuteCommand;
import fr.minuskube.bot.discord.commands.PollCommand;
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
import fr.minuskube.bot.discord.listeners.MuteListener;
import fr.minuskube.bot.discord.trello.TCPServer;
import fr.minuskube.bot.discord.util.Webhook;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Game;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);

    public static final Message UNKNOWN_COMMAND = new MessageBuilder()
            .appendString("Sorry, this command is ")
            .appendString("unknown", MessageBuilder.Formatting.BOLD)
            .appendString(". Use $help to see available commands.").build();

    private static DiscordBot instance;

    private JDA client;
    private LocalDateTime launchTime;

    private Config config = new Config();
    private Gson gson = new Gson();

    private void loadConfig() {
        LOGGER.info("Loading config...");
        File configFile = new File("config.txt");

        if(!configFile.exists()) {
            LOGGER.warn("Config file doesn't not exist, creating a new one.");

            try {
                config.saveDefault(configFile);
            } catch(IOException e) {
                LOGGER.error("Error while creating the default file:", e);
            }
        }
        else
            config.load(configFile);
    }

    public void ready(JDA client) {
        this.client = client;

        LOGGER.info("Starting server...");
        new TCPServer().start();

        LOGGER.info("Registering commands...");
        DiscordBotAPI.registerCommands(
                new HelpCommand(),
                new InfosCommand(),
                new AddCommand(),
                new SuggestCommand(),
                new GifCommand(),
                new QuoteCommand(),
                new FakeQuoteCommand(),
                new GamesCommand(),
                new TestCommand(),
                new StopCommand(),
                new SexCommand(),
                new DrawCommand(),
                new MuteCommand(),
                new PollCommand()
        );

        LOGGER.info("Registering games...");
        DiscordBotAPI.registerGames(
                new NumberGame(),
                new TicTacToeGame(),
                new RPSGame(),
                new ConnectFourGame()
        );

        LOGGER.info("Registering listeners...");
        client.addEventListener(
                new CommandListener(this),
                new GameListener(this),
                new MuteListener(this)
        );

        LOGGER.info("Initializing webhooks...");
        Webhook.initBotHooks();
        LOGGER.info("Initialized " + Webhook.getBotHooks().size() + " webhooks.");

        LOGGER.info("Setting status...");
        client.getPresence().setGame(Game.of("$help - v1.2.2"));

        launchTime = LocalDateTime.now();
        LOGGER.info("MinusBot (Discord) is ready!");
    }

    public void stop() {
        DiscordBotAPI.logout();
        client = null;

        System.exit(0);
    }

    public JDA getClient() { return client; }
    public User getOwner() { return client.getUserById("87941393766420480"); }
    public LocalDateTime getLaunchTime() { return launchTime; }

    public Config getConfig() { return config; }
    public Gson getGson() { return gson; }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            instance = new DiscordBot();
            instance.loadConfig();

            String token = instance.getConfig().getToken();

            if(token != null)
                DiscordBotAPI.login(token);
            else
                LOGGER.error("The 'token' is not set in the config file, can't start.");
        } catch(LoginException | InterruptedException | RateLimitedException e) {
            LOGGER.error("Error while login: ", e);
        }
    }

    public static DiscordBot instance() { return instance; }
}
