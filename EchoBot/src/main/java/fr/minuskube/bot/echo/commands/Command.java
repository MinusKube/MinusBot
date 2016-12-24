package fr.minuskube.bot.echo.commands;

import net.dv8tion.jda.core.entities.Message;

import java.util.ArrayList;
import java.util.List;

public abstract class Command {

    protected String name;
    protected List<String> labels;
    protected String syntax;

    protected String description;
    protected boolean hidden;
    protected boolean guildOnly;

    public Command(String name, List<String> labels, String description, String syntax) {
        this.name = name;
        this.labels = new ArrayList<>(labels);

        this.description = description;
        this.syntax = syntax;
    }

    public Command(String name, String description, String syntax) {
        this(name, new ArrayList<>(), description, syntax);
    }

    public abstract boolean checkSyntax(Message msg, String[] args);
    public abstract void execute(Message msg, String[] args);

    public String getName() { return name; }
    public List<String> getLabels() { return new ArrayList<>(labels); }
    public String getSyntax() { return syntax; }

    public String getDescription() { return description; }
    public boolean isHidden() { return hidden; }
    public boolean isGuildOnly() { return guildOnly; }

}
