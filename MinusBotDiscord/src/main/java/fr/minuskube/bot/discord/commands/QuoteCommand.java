package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.Quote;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
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

        String query = String.join(" ", (CharSequence[]) args);

        channel.getHistory().retrievePast(100).queue(msgs -> {
            msgs.remove(msg);

            List<Message> messages = msgs.stream()
                    .filter(message -> contains(message.getContent(), query)
                            || contains(message.getRawContent(), query))
                    .limit(5).collect(Collectors.toList());

            if(!messages.isEmpty()) {
                Quote quote = new Quote(guild.getMember(msg.getAuthor()), channel, new ArrayList<Message>(messages));
                quote.send();
            } else {
                MessageUtils.error(channel, "No message found...").queue();
            }

            if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                msg.deleteMessage().queue();
        });
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
