package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.DiscordBot;
import org.json.JSONObject;

import java.io.Serializable;

public class Attachment implements Serializable {

    private String id;
    private String name;
    private String url;

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUrl() { return url; }

    public static Attachment from(JSONObject obj) {
        return DiscordBot.instance().getGson().fromJson(obj.toString(), Attachment.class);
    }

}
