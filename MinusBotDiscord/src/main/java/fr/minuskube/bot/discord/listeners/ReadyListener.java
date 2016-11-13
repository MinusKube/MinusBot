package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import net.dv8tion.jda.core.events.ReadyEvent;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class ReadyListener extends ListenerAdapter {

    @Override
    public void onReady(ReadyEvent e) {
        DiscordBot.instance().ready(e.getJDA());
    }

}
