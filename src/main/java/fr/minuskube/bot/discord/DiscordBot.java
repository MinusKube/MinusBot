package fr.minuskube.bot.discord;

import com.google.gson.Gson;
import fr.minuskube.bot.discord.comics.CommitStrip;
import fr.minuskube.bot.discord.commands.*;
import fr.minuskube.bot.discord.games.*;
import fr.minuskube.bot.discord.listeners.*;
import fr.minuskube.bot.discord.trello.TCPServer;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class DiscordBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(DiscordBot.class);
    public static final String PRIVATE_NOT_ALLOWED = "You can't do this in a private channel.";

    private static DiscordBot instance;

    private JDA client;
    private LocalDateTime launchTime;

    private final Config config = new Config();
    private final Gson gson = new Gson();
    private CommitStrip commitStrip;

    private void loadConfig() {
        LOGGER.info("Loading config...");
        File configFile = new File("config.txt");

        if(!configFile.exists()) {
            LOGGER.warn("Config file doesn't not exist, creating a new one.");

            try {
                config.saveDefault(configFile);
            } catch(IOException e) {
                LOGGER.error("Error while creating the default file: ", e);
            }
        }
        else
            config.load(configFile);
    }

    public void ready(JDA client) {
        this.client = client;

        LOGGER.info("Connected on " + client.getGuilds().size() + " guilds with "
                + client.getUsers().size() + " users!");

        for(Guild guild : client.getGuilds())
            LOGGER.info("  - " + guild.getName() + " (" + guild.getMembers().size() + " users)");

        LOGGER.info("Starting server...");
        new TCPServer().start();

        LOGGER.info("Starting CommitStrip timer...");
        commitStrip = new CommitStrip();
        commitStrip.load();
        commitStrip.start();

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
                new DrawCommand(),
                new MuteCommand(),
                new PollCommand(),
                new ClearCommand(),
                new ComicsCommand(),
                new TextCommand(),
                new PresetCommand()
        );

        LOGGER.info("Registering games...");
        DiscordBotAPI.registerGames(
                new NumberGame(),
                new TicTacToeGame(),
                new RPSGame(),
                new ConnectFourGame(),
                new BoxesGame()
        );

        LOGGER.info("Registering listeners...");
        client.addEventListener(
                new CommandListener(this),
                new GameListener(this),
                new MuteListener(this),
                new PollListener(this),
                new QuoteListener(this)
        );

        LOGGER.info("Setting status...");
        client.getPresence().setPresence(Activity.listening(DiscordBotAPI.prefix() + "help - v1.8.1"), false);

        launchTime = LocalDateTime.now();
        LOGGER.info("MinusBot (Discord) is ready!");
    }

    public void stop() {
        if (commitStrip != null) {
            commitStrip.cancel();
        }

        DiscordBotAPI.logout();
        client = null;

        System.exit(0);
    }

    public JDA getClient() { return client; }
    public User getOwner() { return client.getUserById("87941393766420480"); }
    public LocalDateTime getLaunchTime() { return launchTime; }

    public Config getConfig() { return config; }
    public Gson getGson() { return gson; }
    public CommitStrip getCommitStrip() { return commitStrip; }

    public static void main(String[] args) {
        try {
            instance = new DiscordBot();
            instance.loadConfig();

            String token = instance.getConfig().getToken();

            if(token != null)
                DiscordBotAPI.login(token);
            else
                LOGGER.error("The 'token' is not set in the config file, can't start.");

            Runtime.getRuntime().addShutdownHook(new Thread(instance::stop));
        } catch(LoginException | InterruptedException e) {
            LOGGER.error("Error while login: ", e);
        }
    }

    public static DiscordBot instance() { return instance; }

}
