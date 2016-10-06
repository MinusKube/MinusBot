package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.Quote;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class QuoteCommand extends Command {

    public QuoteCommand() {
        super("quote", Collections.singletonList("q"), "Quotes someone's message sent in the channel.");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(args.length < 1) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                    .appendString("$quote <message to find>").build());
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

        StringBuilder sb = new StringBuilder();

        for(String arg : args) {
            if(sb.length() > 0)
                sb.append(" ");

            sb.append(arg.replace("\n", " ").replace("\r", " "));
        }

        List<Message> msgs = channel.getHistory().retrieve();

        msgs.remove(0);

        Optional<Message> opMsg = msgs.stream()
                .filter(message -> message.getContent() != null
                        && message.getContent().toLowerCase().replace("\n", " ").replace("\r", " ").trim()
                        .contains(sb.toString().toLowerCase().trim()))
                .findFirst();

        if(opMsg.isPresent()) {
            Message message = opMsg.get();

            OffsetDateTime odt = message.getTime();
            java.util.Date date = Date.from(odt.toInstant());

            Quote quote = new Quote(channel, message.getAuthor(), message.getContent(), date);
            quote.sendImage(msg.getAuthor());

            if(channel.checkPermission(DiscordBotAPI.self(), Permission.MESSAGE_MANAGE))
                msg.deleteMessage();
        }
        else {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("No message could be found...").build());
        }
    }

}
