package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.commands.Command;
import fr.minuskube.bot.discord.commands.MuteCommand;
import fr.minuskube.bot.discord.util.MessageUtils;
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

    public CommandListener(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        Message msg = e.getMessage();

        if(msg.getContent() == null)
            return;
        if(!msg.getContent().startsWith(DiscordBotAPI.prefix()))
            return;
        if(msg.getAuthor().isBot())
            return;
        if(bot.getConfig().isSelf() != (msg.getAuthor() == DiscordBotAPI.self()))
            return;

        if(msg.getChannelType() == ChannelType.TEXT) {
            TextChannel channel = (TextChannel) msg.getChannel();
            Guild guild = channel.getGuild();

            if(MuteCommand.isMuted(guild.getMember(msg.getAuthor()), guild))
                return;

            if(!guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_WRITE)) {
                Message message = new MessageBuilder()
                        .append("Couldn't answer you in channel ")
                        .append(channel.getName(), MessageBuilder.Formatting.ITALICS)
                        .append(", I don't have the permission to write messages.").build();

                msg.getAuthor().openPrivateChannel()
                        .queue(privateChan -> privateChan.sendMessage(message).queue());
                return;
            }
        }

        String content = msg.getRawContent().substring(DiscordBotAPI.prefix().length());

        if(msg.getChannelType() == ChannelType.TEXT)
            LOGGER.info("{} issued command (Guild {}): " + DiscordBotAPI.prefix() + "{}", msg.getAuthor().getName(),
                    ((TextChannel) msg.getChannel()).getGuild().getName(), content);
        else
            LOGGER.info("{} issued command: " + DiscordBotAPI.prefix() + "{}", msg.getAuthor().getName(), content);

        new Thread(() -> {
            String cmdName = content.split(" ")[0];
            String[] args = StringUtils.split(content.substring(cmdName.length()).trim(), " ");

            Command cmd = DiscordBotAPI.getCommand(cmdName);

            if(cmd != null) {
                if(msg.getChannelType() != ChannelType.TEXT && cmd.isGuildOnly()) {
                    MessageUtils.error(msg.getChannel(), DiscordBot.PRIVATE_NOT_ALLOWED).queue();
                    return;
                }

                if(cmd.checkSyntax(msg, args))
                    cmd.execute(msg, args);
                else
                    MessageUtils.error(msg.getChannel(), "Wrong Syntax",
                            DiscordBotAPI.prefix() + cmdName + " " + cmd.getSyntax()).queue();
            }
        }).start();
    }

}
