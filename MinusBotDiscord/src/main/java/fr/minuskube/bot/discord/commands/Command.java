package fr.minuskube.bot.discord.commands;

import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {

    protected String name;
    protected List<String> labels;

    protected String description;
    protected boolean hidden;

    public Command(String name, List<String> labels, String description) {
        this.name = name;
        this.labels = new ArrayList<>(labels);

        this.description = description;
    }

    public Command(String name, String description) {
        this(name, new ArrayList<>(), description);
    }

    public abstract void execute(Message msg, String[] args);

    public String getName() { return name; }
    public List<String> getLabels() { return new ArrayList<>(labels); }

    public String getDescription() { return description; }
    public boolean isHidden() { return hidden; }

}
