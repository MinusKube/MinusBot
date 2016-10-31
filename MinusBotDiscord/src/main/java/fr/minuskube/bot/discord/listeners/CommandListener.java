package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.commands.Command;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Channel;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandListener extends Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

    public CommandListener(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Message msg = e.getMessage();

        if(msg.getContent() == null)
            return;
        if(!msg.getContent().startsWith("$"))
            return;

        if(msg.getChannel() instanceof Channel) {
            Channel channel = (Channel) msg.getChannel();

            if(!channel.checkPermission(bot.getSelf(), Permission.MESSAGE_WRITE)) {
                Message message = new MessageBuilder()
                        .appendString("Couldn't answer you in channel ")
                        .appendString(channel.getName(), MessageBuilder.Formatting.ITALICS)
                        .appendString(", I don't have the permission to write messages.").build();

                msg.getAuthor().getPrivateChannel().sendMessage(message);
                return;
            }
        }

        String content = msg.getRawContent().substring(1);

        LOGGER.info("{} issued command: ${}", msg.getAuthor().getUsername(), content);

        new Thread(() -> {
            String cmdName = content.split(" ")[0];
            String[] args = StringUtils.split(content.substring(cmdName.length()).trim(), " ");

            Command cmd = DiscordBotAPI.getCommand(cmdName);

            if(cmd != null)
                cmd.execute(msg, args);
            else
                msg.getChannel().sendMessage(DiscordBot.UNKNOWN_COMMAND);
        }).start();
    }

}
