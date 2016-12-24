package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.DrawSession;
import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DrawCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(DrawCommand.class);

    private Map<MessageChannel, DrawSession> sessions = new HashMap<>();

    public DrawCommand() {
        super("draw", Collections.emptyList(), "Starts a draw session on the channel.", "");
    }

    @Override
    public void execute(Message msg, String[] args) {
        final String prefix = DiscordBotAPI.prefix();
        MessageChannel channel = msg.getChannel();

        if(msg.getChannelType() == ChannelType.TEXT) {
            TextChannel tc = (TextChannel) channel;

            if(tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_MANAGE))
                msg.deleteMessage().queue();
        }

        if(!sessions.containsKey(channel)) {
            if(args.length != 2) {
                channel.sendMessage(new MessageBuilder()
                        .append("There is no draw session on this channel.\n", MessageBuilder.Formatting.BOLD)
                        .append("Use " + prefix + "draw <width> <height> to create one.").build())
                        .queue();
                return;
            }

            try {
                int width = Integer.parseInt(args[0]);
                int height = Integer.parseInt(args[1]);

                if(width < 0 || width > 600
                        || height < 0 || height > 600) {

                    MessageUtils.error(channel, "The width and the height must be between 0 and 600.").queue();
                    return;
                }

                DrawSession session = new DrawSession(width, height, channel);
                sessions.put(channel, session);

                channel.sendMessage(new MessageBuilder()
                        .append("Draw session created! ", MessageBuilder.Formatting.BOLD)
                        .append(availableCommands()).build())
                        .queue();
            } catch(NumberFormatException e) {
                wrongSyntax(channel, "<width> <height>");
            }

            return;
        }

        DrawSession session = sessions.get(channel);

        if(args.length == 0) {
            channel.sendMessage(new MessageBuilder()
                    .append(availableCommands()).build()).queue();
            return;
        }

        String syntax;

        switch(args[0].toLowerCase()) {
            case "color": {
                syntax = "color <red> <green> <blue> [alpha]";

                if(args.length < 4 || args.length > 5) {
                    wrongSyntax(channel, syntax);
                    return;
                }

                try {
                    int red = Math.max(0, Math.min(255, Integer.parseInt(args[1])));
                    int green = Math.max(0, Math.min(255, Integer.parseInt(args[2])));
                    int blue = Math.max(0, Math.min(255, Integer.parseInt(args[3])));
                    int alpha = 255;

                    if(args.length == 5)
                        alpha = Math.max(0, Math.min(255, Integer.parseInt(args[4])));

                    session.color(red, green, blue, alpha);
                    session.sendColor();
                } catch(NumberFormatException e) {
                    wrongSyntax(channel, syntax);
                }

                break;
            }

            case "line": {
                syntax = "line <x1> <y1> <x2> <y2> [line-width]";

                if(args.length < 5 || args.length > 6) {
                    wrongSyntax(channel, syntax);
                    return;
                }

                try {
                    int x1 = Integer.parseInt(args[1]);
                    int y1 = Integer.parseInt(args[2]);
                    int x2 = Integer.parseInt(args[3]);
                    int y2 = Integer.parseInt(args[4]);

                    if(args.length == 6) {
                        int lineWidth = Integer.parseInt(args[5]);
                        session.drawLine(x1, y1, x2, y2, lineWidth);
                    }
                    else
                        session.drawLine(x1, y1, x2, y2);

                    session.send();
                } catch(NumberFormatException e) {
                    wrongSyntax(channel, syntax);
                }

                break;
            }

            case "rect": {
                syntax = "rect <x> <y> <width> <height>";

                if(args.length != 5) {
                    wrongSyntax(channel, syntax);
                    return;
                }

                try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int width = Integer.parseInt(args[3]);
                    int height = Integer.parseInt(args[4]);

                    session.fillRect(x, y, width, height);
                    session.send();
                } catch(NumberFormatException e) {
                    wrongSyntax(channel, syntax);
                }

                break;
            }

            case "hrect": {
                syntax = "hrect <x> <y> <width> <height> [line-width]";

                if(args.length < 5 || args.length > 6) {
                    wrongSyntax(channel, syntax);
                    return;
                }

                try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int width = Integer.parseInt(args[3]);
                    int height = Integer.parseInt(args[4]);

                    if(args.length == 6) {
                        int lineWidth = Integer.parseInt(args[5]);
                        session.drawRect(x, y, width, height, lineWidth);
                    }
                    else
                        session.drawRect(x, y, width, height);

                    session.send();
                } catch(NumberFormatException e) {
                    wrongSyntax(channel, syntax);
                }

                break;
            }

            case "circle": {
                syntax = "circle <x> <y> <width> <height>";

                if(args.length != 5) {
                    wrongSyntax(channel, syntax);
                    return;
                }

                try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int width = Integer.parseInt(args[3]);
                    int height = Integer.parseInt(args[4]);

                    session.fillCircle(x, y, width, height);
                    session.send();
                } catch(NumberFormatException e) {
                    wrongSyntax(channel, syntax);
                }

                break;
            }

            case "hcircle": {
                syntax = "hcircle <x> <y> <width> <height> [line-width]";

                if(args.length < 5 || args.length > 6) {
                    wrongSyntax(channel, syntax);
                    return;
                }

                try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    int width = Integer.parseInt(args[3]);
                    int height = Integer.parseInt(args[4]);

                    if(args.length == 6) {
                        int lineWidth = Integer.parseInt(args[5]);
                        session.drawCircle(x, y, width, height, lineWidth);
                    }
                    else
                        session.drawCircle(x, y, width, height);

                    session.send();
                } catch(NumberFormatException e) {
                    wrongSyntax(channel, syntax);
                }

                break;
            }

            case "text": {
                syntax = "text <size> <x> <y> <text>";

                if(args.length < 5) {
                    wrongSyntax(channel, syntax);
                    return;
                }

                try {
                    int size = Integer.parseInt(args[1]);
                    int x = Integer.parseInt(args[2]);
                    int y = Integer.parseInt(args[3]);

                    StringBuilder sb = new StringBuilder();

                    for(int i = 4; i < args.length; i++) {
                        if(sb.length() != 0)
                            sb.append(" ");

                        sb.append(args[i]);
                    }

                    session.drawString(size, x, y, sb.toString());
                    session.send();
                } catch(NumberFormatException e) {
                    wrongSyntax(channel, syntax);
                }

                break;
            }

            case "image": {
                syntax = "image <x> <y> <url> [width] [height]";

                if(args.length != 4 && args.length != 6) {
                    wrongSyntax(channel, syntax);
                    return;
                }

                try {
                    int x = Integer.parseInt(args[1]);
                    int y = Integer.parseInt(args[2]);
                    String url = args[3];

                    if(args.length == 6) {
                        int width = Integer.parseInt(args[4]);
                        int height = Integer.parseInt(args[5]);

                        session.drawImage(x, y, url, width, height);
                    }
                    else
                        session.drawImage(x, y, url);

                    session.send();
                } catch(NumberFormatException e) {
                    wrongSyntax(channel, syntax);
                } catch(IOException e) {
                    MessageUtils.error(channel, "Can't retrieve the image from the given url!")
                            .queue();
                    LOGGER.debug("Can't retrieve image from given url: ", e);
                }

                break;
            }

            case "show": {
                session.send();
                break;
            }

            case "cancel": {
                if(session.cancel())
                    session.send();
                else {
                    MessageUtils.error(channel, "Sorry, I can't cancel the last change...").queue();
                    return;
                }

                break;
            }

            case "reset": {
                session.reset();

                channel.sendMessage(new MessageBuilder()
                        .append("The session has been reset.").build())
                        .queue();
                break;
            }

            case "stop": {
                sessions.remove(channel);

                channel.sendMessage(new MessageBuilder()
                        .append("Session stopped for this channel!", MessageBuilder.Formatting.BOLD).build())
                        .queue();
                break;
            }

            default: {
                MessageUtils.error(channel, "Unknown command, type " + prefix + "draw to see the available commands.")
                        .queue();
                break;
            }
        }
    }

    private String availableCommands() {
        String p = DiscordBotAPI.prefix();

        return "Available commands:\n" +
                "```" +
                p + "draw color <red> <green> <blue> [alpha]\n" +
                p + "draw line <x1> <y1> <x2> <y2> [line-width]\n" +
                p + "draw rect <x> <y> <width> <height>\n" +
                p + "draw hrect <x> <y> <width> <height> [line-width]\n" +
                p + "draw circle <x> <y> <width> <height>\n" +
                p + "draw hcircle <x> <y> <width> <height> [line-width]\n" +
                p + "draw image <x> <y> <url> [width] [height]\n" +
                p + "draw text <size> <x> <y> <text>\n" +
                p + "draw show\n" +
                p + "draw cancel\n" +
                p + "draw reset\n" +
                p + "draw stop" +
                "```";
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) { return true; }

    private void wrongSyntax(MessageChannel channel, String syntax) {
        MessageUtils.error(channel, "Wrong Syntax",
                DiscordBotAPI.prefix() + "draw " + syntax).queue();
    }

}
