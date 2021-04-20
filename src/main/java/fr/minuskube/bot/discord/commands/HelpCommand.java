package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

public class HelpCommand extends Command {

    public HelpCommand() {
        super("help", "Shows the list of available commands.", "");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageBuilder builder = new MessageBuilder()
                .append("Commands:", MessageBuilder.Formatting.BOLD).append("\n");

        for(Command cmd : DiscordBotAPI.getCommands()) {
            if(cmd.isHidden())
                continue;

            builder.append("\n  - " + DiscordBotAPI.prefix() + cmd.getName() + " : ").
                    append(cmd.getDescription(), MessageBuilder.Formatting.ITALICS);
        }

        msg.getChannel().sendMessage(builder.build()).queue();
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }
}
