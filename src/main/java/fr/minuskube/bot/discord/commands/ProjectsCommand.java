package fr.minuskube.bot.discord.commands;

import net.dv8tion.jda.core.entities.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

public class ProjectsCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(ProjectsCommand.class);

    public ProjectsCommand() {
        super("projects", Collections.singletonList("project"),
                "Lists the projects that I support!", "");
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(args.length == 0) {

            return;
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }
}
