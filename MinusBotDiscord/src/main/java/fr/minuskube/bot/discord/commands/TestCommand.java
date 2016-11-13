package fr.minuskube.bot.discord.commands;

import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(TestCommand.class);

    public TestCommand() {
        super("test", "Command for debugging.");

        this.hidden = true;
    }

    @Override
    public void execute(Message msg, String[] args) {}

}
