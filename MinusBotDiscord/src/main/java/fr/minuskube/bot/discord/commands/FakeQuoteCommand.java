package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.Messages;
import fr.minuskube.bot.discord.util.Quote;
import fr.minuskube.bot.discord.util.Users;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
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
                    .appendString("$fakequote <user [#0123]> <message>").build())
                    .queue();
            return;
        }

        if(msg.getChannelType() != ChannelType.TEXT) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("You can't make quotes in private channels.").build())
                    .queue();
            return;
        }

        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();

        if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.deleteMessage().queue();

        Member member = null;

        List<Member> mentions = Messages.getMemberMentions(guild, args[0]);

        if(mentions.size() == 1)
            member = mentions.get(0);

        if(member == null) {
            if(args[0].contains("#"))
                member = Users.search(guild, args[0].split("#")[0], args[0].split("#")[1]);
            else
                member = Users.search(guild, args[0]);
        }

        if(member != null) {
            String message = "";

            for(int i = 1; i < args.length; i++)
                message += args[i] + " ";

            Quote quote = new Quote(channel, member, message, new Date());
            quote.send();

            if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                msg.deleteMessage().queue();
        }
        else {
            channel.sendMessage(new MessageBuilder()
                    .appendString("User not found!").build())
                    .queue();
        }
    }

}
