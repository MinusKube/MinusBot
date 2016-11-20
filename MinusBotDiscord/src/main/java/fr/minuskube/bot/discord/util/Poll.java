package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;

import java.util.HashMap;
import java.util.Map;

public class Poll {

    private TextChannel channel;
    private Map<User, Integer> votes = new HashMap<>();

    public Poll(TextChannel channel) {
        this.channel = channel;
    }

    public boolean hasVoted(User user) { return votes.containsKey(user); }

    public void vote(User user, int value) {
        if(hasVoted(user))
            return;

        votes.put(user, value);
        send();
    }

    public void send() {
        channel.sendMessage("HI BOYZZZZ");
    }

    public TextChannel getChannel() { return channel; }

}
