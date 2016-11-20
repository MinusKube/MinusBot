package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.Message;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Shows the list of available commands.", "");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageBuilder builder = new MessageBuilder()
                .appendString("Commands:", MessageBuilder.Formatting.BOLD).appendString("\n");

        for(Command cmd : DiscordBotAPI.getCommands()) {
            if(cmd.isHidden())
                continue;

            builder.appendString("\n  - " + DiscordBotAPI.prefix() + cmd.getName() + " : ").
                    appendString(cmd.getDescription(), MessageBuilder.Formatting.ITALICS);
        }

        msg.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }
}
