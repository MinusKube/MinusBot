package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.commands.Command;
import fr.minuskube.bot.discord.commands.MuteCommand;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
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
        if(msg.isPrivate())
            return;

        if(MuteCommand.isMuted(msg.getAuthor(), ((TextChannel) msg.getChannel()).getGuild()))
            msg.deleteMessage();
    }

}
