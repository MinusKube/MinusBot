package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.Quote;
import fr.minuskube.bot.discord.util.Users;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.Collections;
import java.util.Date;

public class FakeQuoteCommand extends Command {

    public FakeQuoteCommand() {
        super("fakequote", Collections.singletonList("fq"), "Create a fake quote with a given player and message.");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(args.length < 1) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                    .appendString("$fakequote <player [#0123]> <message").build());
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

        User user;

        if(args[0].contains("#"))
            user = Users.search(channel.getGuild(), args[0].split("#")[0], args[0].split("#")[1]);
        else
            user = Users.search(channel.getGuild(), args[0]);

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
