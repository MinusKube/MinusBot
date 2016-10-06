package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Message;

public class StopCommand extends Command {

    public StopCommand() {
        super("stop", "Shutdowns the bot.");

        this.hidden = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(msg.getAuthor() != DiscordBot.instance().getOwner()) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("You don't have the permission to execute this command...",
                            MessageBuilder.Formatting.ITALICS).build());
            return;
        }

        msg.getChannel().sendMessage(new MessageBuilder()
                .appendString("Goodbye!", MessageBuilder.Formatting.BOLD,
                        MessageBuilder.Formatting.ITALICS).build());

        DiscordBotAPI.stop();
    }

}
