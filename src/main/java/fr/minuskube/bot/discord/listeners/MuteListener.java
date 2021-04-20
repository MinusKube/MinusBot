package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.commands.MuteCommand;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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

        if(msg.getChannelType() != ChannelType.TEXT)
            return;

        Guild guild = ((TextChannel) msg.getChannel()).getGuild();

        if(MuteCommand.isMuted(guild.retrieveMember(msg.getAuthor()).complete(), guild))
            msg.delete().queue();
    }

}
