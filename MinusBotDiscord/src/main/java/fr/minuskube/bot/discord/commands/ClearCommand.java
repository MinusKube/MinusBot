package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

public class ClearCommand extends Command {

    public ClearCommand() {
        super("clear", "Clears the chat (Deletes the X last messages).", "<amount>");

        this.hidden = true;
        this.guildOnly = true;
    }

    public void execute(Message msg, String[] args) {
        if(msg.getAuthor() != DiscordBot.instance().getOwner()) {
            MessageUtils.error(msg.getChannel(), "*You don't have the permission to execute this command...*").queue();
            return;
        }

        TextChannel channel = (TextChannel) msg.getChannel();
        int input = Integer.parseInt(args[0]);

        if(input < 1 || input > 500) {
            MessageUtils.error(channel, "The amount must be between 1 and 500.").queue();
            return;
        }

        channel.getHistory().retrievePast(input).queue(msgs -> channel.deleteMessages(msgs).queue());
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) {
        if(args.length != 1)
            return false;

        try { Integer.parseInt(args[0]); return true; }
        catch(NumberFormatException e) { return false; }
    }

}
