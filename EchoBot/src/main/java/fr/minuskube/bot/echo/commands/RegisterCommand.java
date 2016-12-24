package fr.minuskube.bot.echo.commands;

import fr.minuskube.bot.echo.EchoBot;
import fr.minuskube.bot.echo.EchoBotAPI;
import fr.minuskube.bot.echo.util.MessageUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.Role;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RegisterCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(RegisterCommand.class);

    public RegisterCommand() {
        super("register", "", "");

        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member member = guild.getMember(msg.getAuthor());

        Role role = guild.getRoleById(EchoBot.instance().getConfig().getRegisterRole());
        LOGGER.debug(EchoBot.instance().getConfig().getRegisterRole());

        if(guild.getMember(EchoBotAPI.self()).hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.deleteMessage().queue();

        if(role == null) {
            MessageUtils.error(channel, "Error in the configuration of the bot, the role can not be found.")
                    .queue(msg_ -> Executors.newScheduledThreadPool(1)
                            .schedule((Runnable) msg_.deleteMessage()::queue, 5, TimeUnit.SECONDS));
            return;
        }

        if(member.getRoles().contains(role)) {
            MessageUtils.error(channel, "You are already registered!")
                    .queue(msg_ -> Executors.newScheduledThreadPool(1)
                            .schedule((Runnable) msg_.deleteMessage()::queue, 5, TimeUnit.SECONDS));
            return;
        }

        guild.getController().addRolesToMember(member, role).queue(v ->
                channel.sendMessage("You are now registered!")
                        .queue(msg_ -> Executors.newScheduledThreadPool(1)
                        .schedule((Runnable) msg_.deleteMessage()::queue, 5, TimeUnit.SECONDS)));
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

}
