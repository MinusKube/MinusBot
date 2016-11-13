package fr.minuskube.bot.discord.util;

import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import org.json.JSONObject;

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
        Webhook webhook = Webhook.getBotHook(channel);

        if(webhook == null) {
            channel.sendMessage("Error while creating quote!");
            return;
        }

        webhook.execute(new JSONObject(new HashMap<String, Object>() {{

        }}));
    }

    public TextChannel getChannel() { return channel; }

}
