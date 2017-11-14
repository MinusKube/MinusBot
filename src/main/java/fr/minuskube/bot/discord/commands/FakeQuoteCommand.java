package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.Quote;
import fr.minuskube.bot.discord.util.Users;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FakeQuoteCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(FakeQuoteCommand.class);

    public FakeQuoteCommand() {
        super("fakequote", Collections.singletonList("fq"), "Create a fake quote with a given player and message.",
                "<user [#0123]> <message>");

        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();

        if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.delete().queue();

        Member member = null;

        List<Member> mentions = MessageUtils.getMemberMentions(guild, args[0]);

        if(mentions.size() == 1)
            member = mentions.get(0);

        if(member == null) {
            if(args[0].contains("#"))
                member = Users.search(guild, args[0].split("#")[0], args[0].split("#")[1]);
            else
                member = Users.search(guild, args[0]);
        }

        if(member != null) {
            String[] queryArgs = Arrays.copyOfRange(args, 1, args.length);
            String query = String.join(" ", (CharSequence[]) queryArgs);

            Quote quote = new Quote(guild.getMember(msg.getAuthor()), channel, member, query, new Date());
            quote.send();
        }
        else {
            MessageUtils.error(channel, "User not found!").queue();
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) {
        return args.length >= 1;
    }
}
