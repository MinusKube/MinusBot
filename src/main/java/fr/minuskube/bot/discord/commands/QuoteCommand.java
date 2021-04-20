package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.Quote;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuoteCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteCommand.class);

    public QuoteCommand() {
        super("quote", Collections.singletonList("q"), "Quotes someone's message sent in the channel.",
                "<query>");

        this.guildOnly = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member member = guild.retrieveMember(msg.getAuthor()).complete();

        String query = String.join(" ", (CharSequence[]) args);

        List<Message> msgs = channel.getHistory().retrievePast(100).complete();
        msgs.remove(msg);

        List<Message> messages = msgs.stream()
                .filter(message -> contains(message.getContentDisplay(), query)
                        || contains(message.getContentRaw(), query))
                .limit(5).collect(Collectors.toList());

        if(!messages.isEmpty()) {
            Quote quote = new Quote(member, channel, new ArrayList<Message>(messages));
            quote.send();
        } else {
            MessageUtils.error(channel, "No message found...").queue();
        }

        if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.delete().queue();
    }

    private boolean contains(String container, String msg) {
        return !(msg == null || container == null) &&
                container.toLowerCase().replace("\n", " ").replace("\r", " ").trim()
                        .contains(msg.toLowerCase().replace("\n", " ").replace("\r", " ").trim());

    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) {
        return args.length >= 1;
    }

}
