package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.Quote;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

public class QuoteCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteCommand.class);

    public QuoteCommand() {
        super("quote", Collections.singletonList("q"), "Quotes someone's message sent in the channel.");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(args.length < 1) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                    .appendString("$quote <message to find>").build())
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

        StringBuilder sb = new StringBuilder();

        for(String arg : args) {
            if(sb.length() > 0)
                sb.append(" ");

            sb.append(arg.replace("\n", " ").replace("\r", " "));
        }

        channel.getHistory().retrievePast(100).queue(msgs -> {
            msgs.remove(0);

            Optional<Message> opMsg = msgs.stream()
                    .filter(message -> message.getContent() != null
                            && message.getContent().toLowerCase().replace("\n", " ").replace("\r", " ").trim()
                            .contains(sb.toString().toLowerCase().trim()))
                    .findFirst();

            if(opMsg.isPresent()) {
                Message message = opMsg.get();

                OffsetDateTime odt = message.getCreationTime();
                java.util.Date date = Date.from(odt.toInstant());

                Quote quote = new Quote(channel, guild.getMember(message.getAuthor()), message.getContent(), date);
                quote.send();
            } else {
                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString("No message could be found...").build())
                        .queue();
            }

            if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                msg.deleteMessage().queue();
        });
    }

}
