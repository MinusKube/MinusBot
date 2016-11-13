package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.commands.MuteCommand;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MuteListener extends Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MuteListener.class);

    public MuteListener(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Message msg = e.getMessage();

        if(msg.getContent() == null)
            return;
        if(msg.getChannelType() != ChannelType.TEXT)
            return;

        if(MuteCommand.isMuted(msg.getAuthor(), ((TextChannel) msg.getChannel()).getGuild()))
            msg.deleteMessage().queue();
    }

}
