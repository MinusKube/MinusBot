package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.util.MessageUtils;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ClearCommand extends Command {

    public ClearCommand() {
        super("clear", "Clears the chat (Deletes the X last messages).", "<amount>");

        this.hidden = true;
        this.guildOnly = true;
    }

    public void execute(Message msg, String[] args) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member author = guild.getMember(msg.getAuthor());

        if(!author.hasPermission(channel, Permission.ADMINISTRATOR)) {
            MessageUtils.error(channel, "*You don't have the permission to execute this command...*").queue();
            return;
        }

        int input = Integer.parseInt(args[0]);

        if(input < 1 || input > 500) {
            MessageUtils.error(channel, "The amount must be between 1 and 500.").queue();
            return;
        }

        if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            msg.deleteMessage().queue();

        String[] queryArgs = Arrays.copyOfRange(args, 1, args.length);
        String query = String.join(" ", (CharSequence[]) queryArgs);

        List<Member> mentions = MessageUtils.getMemberMentions(guild, query);

        channel.getHistory().retrievePast(input).queue(msgs -> {
            if(!mentions.isEmpty()) {
                for(Message m : new ArrayList<Message>(msgs)) {
                    Member member = guild.getMember(m.getAuthor());

                    if(!mentions.contains(member))
                        msgs.remove(m);
                }
            }

            if(msgs.size() > 1)
                channel.deleteMessages(msgs).queue();
            else if(!msgs.isEmpty())
                msgs.get(0).deleteMessage().queue();
        });
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) {
        if(args.length < 1)
            return false;

        try { Integer.parseInt(args[0]); return true; }
        catch(NumberFormatException e) { return false; }
    }

}
