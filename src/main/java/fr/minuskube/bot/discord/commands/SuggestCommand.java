package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

public class SuggestCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(SuggestCommand.class);

    private Map<User, Integer> suggestionsAmount = new HashMap<User, Integer>();

    public SuggestCommand() {
        super("suggest", "Suggests a thing my developer can add to me.", "<message>");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(suggestionsAmount.getOrDefault(msg.getAuthor(), 0) >= 5) {
            MessageUtils.error(msg.getChannel(), "You've already sent too many suggestions today, try again later...")
                    .queue();
            return;
        }

        StringBuilder sb = new StringBuilder();

        for(String arg : args)
            sb.append(" ").append(arg.replace("\n", " ").replace("\r", " "));

        LOGGER.info("{} made a suggestion: {}", msg.getAuthor().getName(), sb.toString());

        try {
            String line = msg.getAuthor().getName()
                    + "#" + msg.getAuthor().getDiscriminator() + " >" + sb.toString();
            Path path = Paths.get("suggestions.txt");

            if(!Files.exists(path)) {
                Files.createFile(path);
                Files.write(path, line.getBytes(), StandardOpenOption.APPEND);
            }
            else
                Files.write(path, ("\n" + line).getBytes(), StandardOpenOption.APPEND);
        } catch (IOException e) {
            LOGGER.error("Couldn't save suggestions:", e);
        }

        suggestionsAmount.put(msg.getAuthor(), suggestionsAmount.getOrDefault(msg.getAuthor(), 0) + 1);

        msg.getChannel().sendMessage(new MessageBuilder()
                .append("Thank you for your suggestion ").append(msg.getAuthor())
                .append("!").build())
                .queue();

        DiscordBot.instance().getOwner().openPrivateChannel().queue(privateChan ->
                privateChan.sendMessage(new MessageBuilder()
                        .append(msg.getAuthor())
                        .append(" made a suggestion for me!", MessageBuilder.Formatting.BOLD)
                        .append("\n")
                        .append("Content: ", MessageBuilder.Formatting.BOLD)
                        .append(sb.toString()).build())
                        .queue()
        );
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) {
        return args.length >= 1;
    }

}
