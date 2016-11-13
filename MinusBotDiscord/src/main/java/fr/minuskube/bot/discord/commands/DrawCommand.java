package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.DrawSession;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DrawCommand extends Command {

    private Map<MessageChannel, DrawSession> sessions = new HashMap<>();

    private static final String AVAILABLE_COMMANDS_MSG = "Available commands:\n" +
            "```" +
            "$draw color <red> <green> <blue> [alpha]\n" +
            "(SOON) $draw line <x1> <y1> <x2> <x2> [line-width]\n" +
            "$draw rect <x> <y> <width> <height>\n" +
            "$draw hrect <x> <y> <width> <height> [line-width]\n" +
            "$draw circle <x> <y> <width> <height>\n" +
            "$draw hcircle <x> <y> <width> <height> [line-width]\n" +
            "(SOON) $draw image <x> <y> <width> <height> <url>\n" +
            "$draw text <size> <x> <y> <text>\n" +
            "$draw show\n" +
            "$draw cancel\n" +
            "$draw reset\n" +
            "$draw stop" +
            "```";

    public DrawCommand() {
        super("draw", Collections.emptyList(), "Starts a draw session on the channel.");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();

        if(msg.getChannelType() == ChannelType.TEXT) {
            TextChannel tc = (TextChannel) channel;

            if(tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_MANAGE))
                msg.deleteMessage().queue();
        }

        if(!sessions.containsKey(channel)) {
            if(args.length != 2) {
                channel.sendMessage(new MessageBuilder()
                        .appendString("There is no draw session on this channel.\n", MessageBuilder.Formatting.BOLD)
                        .appendString("Use $draw <width> <height> to create one.").build())
                        .queue();
                return;
            }

            try {
                int width = Integer.parseInt(args[0]);
                int height = Integer.parseInt(args[1]);

                if(width < 0 || width > 600 || height < 0 || height > 600) {
                    channel.sendMessage(new MessageBuilder()
                            .appendString("The width and the height must be between 0 and 600.").build())
                            .queue();
                    return;
                }

                DrawSession session = new DrawSession(width, height, channel);
                sessions.put(channel, session);

                channel.sendMessage(new MessageBuilder()
                        .appendString("Draw session created!", MessageBuilder.Formatting.BOLD)
                        .appendString(AVAILABLE_COMMANDS_MSG).build())
                        .queue();
            } catch(NumberFormatException e) {
                channel.sendMessage(new MessageBuilder()
                        .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                        .appendString("$draw <width> <height>").build())
                        .queue();
            }

            return;
        }

        DrawSession session = sessions.get(channel);

        if(args.length == 0) {
            channel.sendMessage(new MessageBuilder()
                    .appendString(AVAILABLE_COMMANDS_MSG).build()).queue();
            return;
        }

        switch(args[0].toLowerCase()) {
            case "color": {
                if(args.length < 4 || args.length > 5) {
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw color <red> <green> <blue> [alpha]").build())
                            .queue();
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
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw color <red> <green> <blue> [alpha]")
                            .build());
                }

                break;
            }

            case "rect": {
                if(args.length != 5) {
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw rect <x> <y> <width> <height>").build())
                            .queue();
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
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw rect <x> <y> <width> <height>").build())
                            .queue();
                }

                break;
            }

            case "hrect": {
                if(args.length < 5 || args.length > 6) {
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw hrect <x> <y> <width> <height> [line-width]").build())
                            .queue();
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
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw hrect <x> <y> <width> <height> [line-width]").build())
                            .queue();
                }

                break;
            }

            case "circle": {
                if(args.length != 5) {
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw circle <x> <y> <width> <height>").build())
                            .queue();
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
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw circle <x> <y> <width> <height>").build())
                            .queue();
                }

                break;
            }

            case "hcircle": {
                if(args.length < 5 || args.length > 6) {
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw hcircle <x> <y> <width> <height> [line-width]").build())
                            .queue();
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
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw hcircle <x> <y> <width> <height> [line-width]").build())
                            .queue();
                }

                break;
            }

            case "text": {
                if(args.length < 5) {
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw text <size> <x> <y> <text>").build())
                            .queue();
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
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                            .appendString("$draw text <size> <x> <y> <text>").build())
                            .queue();
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
                    channel.sendMessage(new MessageBuilder()
                            .appendString("Sorry, I can't cancel this...").build())
                            .queue();
                    return;
                }

                break;
            }

            case "reset": {
                session.reset();

                channel.sendMessage(new MessageBuilder()
                        .appendString("The session has been reset...").build())
                        .queue();
                break;
            }

            case "stop": {
                sessions.remove(channel);

                channel.sendMessage(new MessageBuilder()
                        .appendString("Session stopped for this channel!", MessageBuilder.Formatting.BOLD).build())
                        .queue();
                break;
            }

            default: {
                channel.sendMessage(new MessageBuilder()
                        .appendString("Unknown command, type $draw to see the available commands.").build())
                        .queue();
                break;
            }
        }
    }

}
