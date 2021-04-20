package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public abstract class Listener extends ListenerAdapter {

    protected final DiscordBot bot;

    public Listener(DiscordBot bot) {
        this.bot = bot;
    }

}
