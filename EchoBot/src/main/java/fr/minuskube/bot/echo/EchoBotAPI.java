package fr.minuskube.bot.echo;

import fr.minuskube.bot.echo.commands.Command;
import fr.minuskube.bot.echo.listeners.ReadyListener;
import net.dv8tion.jda.core.AccountType;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.entities.SelfUser;
import net.dv8tion.jda.core.exceptions.RateLimitedException;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EchoBotAPI {

    private static List<Command> commands = new ArrayList<>();

    public static JDA client() { return EchoBot.instance().getClient(); }
    public static SelfUser self() { return client().getSelfUser(); }
    public static String prefix() { return EchoBot.instance().getConfig().getPrefix(); }

    public static void stop() { EchoBot.instance().stop(); }

    public static JDA login(String token) throws LoginException, InterruptedException, RateLimitedException {
        return new JDABuilder(AccountType.BOT).setToken(token)
                .addListener(new ReadyListener()).buildBlocking();
    }

    public static void logout() {
        client().shutdown();
    }


    public static void  registerCommands(Command... cmds) {
        Collections.addAll(commands, cmds);
    }

    public static Command getCommand(String name) {
        for(Command cmd : commands)
            if(cmd.getName().equalsIgnoreCase(name) || cmd.getLabels().contains(name.toLowerCase()))
                return cmd;

        return null;
    }

    public static List<Command> getCommands() { return commands; }

}
