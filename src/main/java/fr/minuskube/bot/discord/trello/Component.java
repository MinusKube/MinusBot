package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.DiscordBot;
import org.json.JSONObject;

import java.io.Serializable;

public class Component implements Serializable {

    private String id;

    public String getId() { return id; }

    public static Component from(JSONObject obj) {
        return DiscordBot.instance().getGson().fromJson(obj.toString(), Component.class);
    }

}
