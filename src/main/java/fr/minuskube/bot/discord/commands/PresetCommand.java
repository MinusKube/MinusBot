package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.MessageUtils;
import fr.minuskube.bot.discord.util.TabbedList;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class PresetCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(PresetCommand.class);

    private Map<User, Map<String, String>> presets = new HashMap<>();

    public PresetCommand() {
        super("preset", Collections.singletonList("presets"), "Shows or creates message presets.", "");

        load();
    }

    private void load() {
        try {
            Path path = Paths.get("presets.txt");

            if(!Files.exists(path))
                return;

            for(String line : Files.readAllLines(path)) {
                String[] splitted = line.split(":", 3);

                if(splitted.length != 3)
                    continue;

                User user = DiscordBotAPI.client().getUserById(splitted[0]);

                if(user == null)
                    continue;

                String preset = splitted[1];
                String text = splitted[2].replace("\\n", "\n");

                presets.putIfAbsent(user, new HashMap<>());
                presets.get(user).put(preset, text);
            }
        } catch(IOException e) {
            LOGGER.error("Error while loading presets: ", e);
        }
    }

    private void save() {
        try {
            Path path = Paths.get("presets.txt");

            StringBuilder sb = new StringBuilder();

            for(Map.Entry<User, Map<String, String>> entry : presets.entrySet()) {
                for(Map.Entry<String, String> preset : entry.getValue().entrySet()) {
                    if(sb.length() > 0)
                        sb.append("\n");

                    sb.append(entry.getKey().getId()).append(":")
                            .append(preset.getKey()).append(":")
                            .append(preset.getValue().replace("\n", "\\n"));
                }
            }

            Files.write(path, sb.toString().getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);
        } catch(IOException e) {
            LOGGER.error("Error while saving presets: ", e);
        }
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();

        if(channel.getType() == ChannelType.TEXT) {
            TextChannel tc = (TextChannel) channel;
            Guild guild = tc.getGuild();

            if(guild.getSelfMember().hasPermission(tc, Permission.MESSAGE_MANAGE))
                msg.delete().queue();
        }

        if(args.length == 0) {
            channel.sendMessage(availableCommands()).queue();
            return;
        }

        String subCommand = args[0];

        presets.putIfAbsent(msg.getAuthor(), new HashMap<>());

        switch(subCommand) {
            case "list": {
                MessageBuilder builder = new MessageBuilder();

                if(presets.get(msg.getAuthor()).isEmpty()) {
                    builder.append("`No preset`");
                }
                else {
                    builder.append(msg.getAuthor().getName()).append("'s Presets: ")
                            .append(String.join(", ", presets.get(msg.getAuthor()).keySet()));
                }

                channel.sendMessage(builder.build()).queue();
                break;
            }

            case "show": {
                if(args.length != 2) {
                    MessageUtils.error(channel, "Wrong Syntax",
                            DiscordBotAPI.prefix() + "preset show <preset>").queue();
                    return;
                }

                String preset = args[1].toLowerCase();

                if(!presets.get(msg.getAuthor()).containsKey(preset)) {
                    MessageUtils.error(channel, "Unknown Preset",
                            "The given preset doesn't exist").queue();
                    return;
                }

                channel.sendMessage(presets.get(msg.getAuthor()).get(preset)).queue();
                break;
            }

            case "create": {
                if(args.length < 3) {
                    MessageUtils.error(channel, "Wrong Syntax",
                            DiscordBotAPI.prefix() + "preset create <preset> <text>").queue();
                    return;
                }

                String preset = args[1].toLowerCase();
                String text = String.join(" ", ArrayUtils.removeAll(args, 0, 1));

                presets.get(msg.getAuthor()).put(preset, text);
                save();

                channel.sendMessage("The preset `" + preset + "` has successfully been created.").queue();
                break;
            }

            case "delete": {
                if(args.length < 2) {
                    MessageUtils.error(channel, "Wrong Syntax",
                            DiscordBotAPI.prefix() + "preset delete <preset>").queue();
                    return;
                }

                String preset = args[1].toLowerCase();

                if(!presets.get(msg.getAuthor()).containsKey(preset)) {
                    MessageUtils.error(channel, "Unknown Preset",
                            "The given preset doesn't exist").queue();
                    return;
                }

                presets.get(msg.getAuthor()).remove(preset);
                save();

                channel.sendMessage("The preset `" + preset + "` has successfully been deleted.").queue();
                break;
            }

            default:
                channel.sendMessage(availableCommands()).queue();
                break;
        }
    }

    private String availableCommands() {
        String p = DiscordBotAPI.prefix();

        return "```" + new TabbedList(new String[] {
                "Name:", "",
                p + "preset list",
                p + "preset show",
                p + "preset create",
                p + "preset delete"
        }, new String[] {
                "Description:", "",
                "Displays the list of created presets",
                "Shows a previously created preset",
                "Creates a personal preset",
                "Deletes a personal preset"
        }).toString() + "```";
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

}
