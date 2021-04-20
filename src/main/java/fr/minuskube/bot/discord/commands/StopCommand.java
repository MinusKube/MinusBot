package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop", "Shutdowns the bot.", "");

        this.hidden = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(msg.getAuthor() != DiscordBot.instance().getOwner()) {
            MessageUtils.error(msg.getChannel(), "*You don't have the permission to execute this command...*").queue();
            return;
        }

        msg.delete().complete();

        msg.getChannel().sendMessage(new MessageBuilder()
                .append("Goodbye!", MessageBuilder.Formatting.BOLD,
                        MessageBuilder.Formatting.ITALICS).build())
                .complete();

        DiscordBotAPI.stop();
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }
}
