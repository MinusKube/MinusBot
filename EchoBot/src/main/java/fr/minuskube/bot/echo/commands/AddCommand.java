package fr.minuskube.bot.discord.commands;

import net.dv8tion.jda.core.entities.Message;

public class AddCommand extends Command {

    public AddCommand() {
        super("add", "Shows the link to add me on your server.", "");
    }

    @Override
    public void execute(Message msg, String[] args) {
        msg.getChannel().sendMessage("Use this link to add me on your server: " +
                "https://discordapp.com/oauth2/authorize?client_id=182237154964013057&scope=bot").queue();
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

}
