package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PollCreation {

    private static final Logger LOGGER = LoggerFactory.getLogger(PollCreation.class);

    private static Map<TextChannel, PollCreation> creations = new HashMap<>();

    private final Member member;
    private final TextChannel channel;
    private short state = -1;

    private String pollTitle;
    private List<String> pollChoices;

    private List<Message> msgs = new ArrayList<>();

    private TimerTask task;

    public PollCreation(Member member, TextChannel channel) {
        this.member = member;
        this.channel = channel;
    }

    public void start() {
        msgs.add(channel.sendMessage("Please enter the **title** of the poll.").complete());

        state = 0;
        creations.put(channel, this);

        resetTask();
    }

    public void receive(Message msg) {
        msgs.add(msg);

        resetTask();

        switch(state) {
            case 0: {
                pollTitle = msg.getContent();

                msgs.add(channel.sendMessage("Please enter the **first choice** of the poll.").complete());
                break;
            }
            case 1: {
                pollChoices = new ArrayList<>();
                pollChoices.add(msg.getContent());

                msgs.add(channel.sendMessage("Please enter the **second choice** of the poll.").complete());
                break;
            }
            case 2: {
                pollChoices.add(msg.getContent());

                msgs.add(channel.sendMessage("Please enter **other choices** for the poll or **end** to finish" +
                        " the creation...").complete());
                break;
            }
            case 3:
            case 4:
            case 5: {
                if(msg.getContent().equalsIgnoreCase("end")) {
                    end();
                    return;
                }

                pollChoices.add(msg.getContent());

                msgs.add(channel.sendMessage("Please enter **other choices** for the poll or **end** to finish" +
                        " the creation...").complete());
                break;
            }

            default: end(); return;
        }

        state++;
    }

    private void cancel() {
        if(channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            channel.deleteMessages(msgs).queue();

        creations.remove(channel);
    }

    public void end() {
        task.cancel();

        if(channel.getGuild().getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
            channel.deleteMessages(msgs).queue();

        new Poll(member, channel, pollTitle, pollChoices.toArray(new String[pollChoices.size()]))
                .start();

        creations.remove(channel);
    }

    private void resetTask() {
        if(task != null)
            task.cancel();

        task = new TimerTask() {
            @Override
            public void run() {
                PollCreation.this.cancel();
            }
        };

        new Timer().schedule(task, 1000 * 30);
    }

    public Member getMember() { return member; }

    public static Map<TextChannel, PollCreation> getCreations() { return creations; }

}
