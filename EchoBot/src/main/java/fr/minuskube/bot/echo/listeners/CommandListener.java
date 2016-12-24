package fr.minuskube.bot.echo.listeners;

import fr.minuskube.bot.echo.EchoBot;
import fr.minuskube.bot.echo.EchoBotAPI;
import fr.minuskube.bot.echo.commands.Command;
import fr.minuskube.bot.echo.util.MessageUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommandListener extends Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommandListener.class);

    public CommandListener(EchoBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Message msg = e.getMessage();

        if(msg.getContent() == null)
            return;
        if(!msg.getContent().startsWith(EchoBotAPI.prefix()))
            return;

        if(msg.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = (TextChannel) msg.getChannel();
            Guild guild = channel.getGuild();

            if(!guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
                Message message = new MessageBuilder()
                        .appendString("Couldn't answer you in channel ")
                        .appendString(channel.getName(), MessageBuilder.Formatting.ITALICS)
                        .appendString(", I don't have the permission to write messages.").build();

                msg.getAuthor().getPrivateChannel().sendMessage(message).queue();
                return;
            }
        }

        String content = msg.getRawContent().substring(EchoBotAPI.prefix().length());

        if(msg.getChannelType() == ChannelType.TEXT)
            LOGGER.info("{} issued command (Guild {}): " + EchoBotAPI.prefix() + "{}", msg.getAuthor().getName(),
                    ((TextChannel) msg.getChannel()).getGuild().getName(), content);
        else
            LOGGER.info("{} issued command: " + EchoBotAPI.prefix() + "{}", msg.getAuthor().getName(), content);

        new Thread(() -> {
            String cmdName = content.split(" ")[0];
            String[] args = StringUtils.split(content.substring(cmdName.length()).trim(), " ");

            Command cmd = EchoBotAPI.getCommand(cmdName);

            if(cmd != null) {
                if(msg.getChannelType() != ChannelType.TEXT && cmd.isGuildOnly()) {
                    MessageUtils.error(msg.getChannel(), EchoBot.PRIVATE_NOT_ALLOWED).queue();
                    return;
                }

                if(cmd.checkSyntax(msg, args))
                    cmd.execute(msg, args);
                else
                    MessageUtils.error(msg.getChannel(), "Wrong Syntax",
                            EchoBotAPI.prefix() + cmdName + " " + cmd.getSyntax()).queue();
            }
        }).start();
    }

}
