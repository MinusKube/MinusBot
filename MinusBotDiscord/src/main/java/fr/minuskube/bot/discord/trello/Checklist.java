package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.DiscordBot;
import org.json.JSONObject;

import java.io.Serializable;

public class Checklist extends Component implements Serializable {

    private String name;

    public String getName() { return name; }

    public static Checklist from(JSONObject obj) {
        return DiscordBot.instance().getGson().fromJson(obj.toString(), Checklist.class);
    }

}
