package fr.minuskube.bot.echo.listeners;

import fr.minuskube.bot.echo.EchoBot;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public abstract class Listener extends ListenerAdapter {

    protected EchoBot bot;

    public Listener(EchoBot bot) {
        this.bot = bot;
    }

}
