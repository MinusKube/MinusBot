package fr.minuskube.bot.discord.commands;

import net.dv8tion.jda.entities.Message;

public class TestCommand extends Command {

    public TestCommand() {
        super("test", "Command for debugging.");
    }

    @Override
    public void execute(Message msg, String[] args) {}

}
