package fr.minuskube.bot.echo.listeners;

import fr.minuskube.bot.echo.EchoBot;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent e) {
        EchoBot.instance().ready(e.getJDA());
    }

}
