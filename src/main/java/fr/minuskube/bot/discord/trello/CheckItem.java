package fr.minuskube.bot.discord.trello;

import com.google.gson.annotations.SerializedName;
import fr.minuskube.bot.discord.DiscordBot;
import org.json.JSONObject;

import java.io.Serializable;

public class CheckItem extends Component implements Serializable {

    public enum State {
        @SerializedName("incomplete")
        INCOMPLETE("Incomplete"),

        @SerializedName("complete")
        COMPLETE("Complete");

        private String name;

        State(String name) {
            this.name = name;
        }

        public String getName() { return name; }
    }

    private String name;
    private State state;

    public String getName() { return name; }
    public State getState() { return state; }

    public static CheckItem from(JSONObject obj) {
        return DiscordBot.instance().getGson().fromJson(obj.toString(), CheckItem.class);
    }

}
