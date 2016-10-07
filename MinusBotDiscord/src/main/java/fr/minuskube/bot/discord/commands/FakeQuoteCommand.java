package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.Messages;
import fr.minuskube.bot.discord.util.Quote;
import fr.minuskube.bot.discord.util.Users;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FakeQuoteCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeQuoteCommand.class);

    public FakeQuoteCommand() {
        super("fakequote", Collections.singletonList("fq"), "Create a fake quote with a given player and message.");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(args.length < 1) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                    .appendString("$fakequote <user [#0123]> <message>").build());
            return;
        }

        if(!(msg.getChannel() instanceof TextChannel)) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("You can't make quotes in private channels.").build());
            return;
        }

        TextChannel channel = (TextChannel) msg.getChannel();

        if(!channel.checkPermission(DiscordBotAPI.self(), Permission.MESSAGE_ATTACH_FILES)) {
            channel.sendMessage(new MessageBuilder()
                    .appendString("No permission to send files!", MessageBuilder.Formatting.BOLD).build());
            return;
        }

        User user = null;

        List<User> mentions = Messages.getUserMentions(channel.getGuild(), args[0]);

        if(mentions.size() == 1)
            user = mentions.get(0);

        if(user == null) {
            if(args[0].contains("#"))
                user = Users.search(channel.getGuild(), args[0].split("#")[0], args[0].split("#")[1]);
            else
                user = Users.search(channel.getGuild(), args[0]);
        }

        if(user != null) {
            String message = "";

            for(int i = 1; i < args.length; i++)
                message += args[i] + " ";

            Quote quote = new Quote(channel, user, message, new Date());
            quote.sendImage(msg.getAuthor());

            if(channel.checkPermission(DiscordBotAPI.self(), Permission.MESSAGE_MANAGE))
                msg.deleteMessage();
        }
        else {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("User not found!").build());
        }
    }

}
