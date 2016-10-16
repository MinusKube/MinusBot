package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.Messages;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.entities.Guild;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MuteCommand extends Command {

    private static Map<User, List<Guild>> muted = new HashMap<>();

    public MuteCommand() {
        super("mute", "Mutes an user.");

        this.hidden = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        if(msg.getAuthor() != DiscordBot.instance().getOwner()) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("You don't have the permission to execute this command...",
                            MessageBuilder.Formatting.ITALICS).build());
            return;
        }

        if(args.length != 1) {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                    .appendString("/mute <user>").build());
            return;
        }

        if(msg.isPrivate()) {
            msg.getChannel().sendMessage("You can't mute people in private channels...");
            return;
        }

        TextChannel channel = (TextChannel) msg.getChannel();
        List<User> mentions = Messages.getUserMentions(channel.getGuild(), args[0]);

        if(mentions.size() == 1) {
            User u = mentions.get(0);

            if(!muted.containsKey(u) || !muted.get(u).contains(channel.getGuild())) {
                muted.putIfAbsent(u, new ArrayList<>());
                muted.get(u).add(channel.getGuild());

                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString(u.getUsername() + " has been muted.").build());
            }
            else {
                muted.get(u).remove(channel.getGuild());

                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString(u.getUsername() + " has been unmuted.").build());
            }
        }
        else {
            msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Wrong syntax! ", MessageBuilder.Formatting.BOLD)
                    .appendString("/mute <user>").build());
        }
    }

    public static boolean isMuted(User user, Guild guild) { return muted.containsKey(user)
            && muted.get(user).contains(guild); }

}
