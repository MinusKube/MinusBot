package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.DiscordBot;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

public class Card implements Serializable {

    private String id;
    private String idList;
    private int idShort;
    private String name;
    private String desc;
    private String shortLink;
    private Date due;
    private int pos;

    public String getId() { return id; }
    public String getIdList() { return idList; }
    public int getIdShort() { return idShort; }
    public String getName() { return name; }
    public String getDescription() { return desc; }
    public String getShortLink() { return shortLink; }
    public Date getDue() { return due; }
    public int getPosition() { return pos; }

    public static Card from(JSONObject obj) {
        return DiscordBot.instance().getGson().fromJson(obj.toString(), Card.class);
    }

}
