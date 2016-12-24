package fr.minuskube.bot.echo;

import fr.minuskube.bot.echo.commands.RegisterCommand;
import fr.minuskube.bot.echo.listeners.CommandListener;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.exceptions.RateLimitedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.login.LoginException;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

public class EchoBot {

    private static final Logger LOGGER = LoggerFactory.getLogger(EchoBot.class);
    public static final String PRIVATE_NOT_ALLOWED = "You can't do this in a private channel.";

    private static EchoBot instance;

    private JDA client;
    private LocalDateTime launchTime;

    private Config config = new Config();

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

        LOGGER.info("Registering commands...");
        EchoBotAPI.registerCommands(
                new RegisterCommand()
        );

        LOGGER.info("Registering listeners...");
        client.addEventListener(
                new CommandListener(this)
        );

        launchTime = LocalDateTime.now();
        LOGGER.info("EchoBot (Discord) is ready!");
    }

    public void stop() {
        EchoBotAPI.logout();
        client = null;

        System.exit(0);
    }

    public JDA getClient() { return client; }
    public User getOwner() { return client.getUserById("87941393766420480"); }
    public LocalDateTime getLaunchTime() { return launchTime; }

    public Config getConfig() { return config; }

    @SuppressWarnings("deprecation")
    public static void main(String[] args) {
        try {
            instance = new EchoBot();
            instance.loadConfig();

            String token = instance.getConfig().getToken();

            if(token != null)
                EchoBotAPI.login(token);
            else
                LOGGER.error("The 'token' is not set in the config file, can't start.");
        } catch(LoginException | InterruptedException | RateLimitedException e) {
            LOGGER.error("Error while login: ", e);
        }
    }

    public static EchoBot instance() { return instance; }

}
