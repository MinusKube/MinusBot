package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.DiscordBot;
import org.json.JSONObject;

import java.io.Serializable;

public class Member implements Serializable {

    private String id;
    private String username;
    private String fullName;
    private String initials;
    private String avatarHash;

    public String getId() { return id; }
    public String getUsername() { return username; }
    public String getFullName() { return fullName; }
    public String getInitials() { return initials; }
    public String getAvatarHash() { return avatarHash; }
    public String getAvatarURL() { return "https://trello-avatars.s3.amazonaws.com/" + avatarHash + "/original.png"; }

    public static Member from(JSONObject obj) {
        return DiscordBot.instance().getGson().fromJson(obj.toString(), Member.class);
    }

}
