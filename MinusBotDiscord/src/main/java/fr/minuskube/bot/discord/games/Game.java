package fr.minuskube.bot.discord.games;

import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class Game {

    protected String name;
    protected String description;

    protected List<Player> players = new ArrayList<>();
    protected Map<Player, GameData> datas = new HashMap<>();

    protected boolean allowDuplicate = false;
    protected int maxPlayers = 1;

    public Game(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public void start(Player player, TextChannel channel) {
        players.add(player);
    }
    public void end(Player player, TextChannel channel) {
        players.remove(player);
        player.delete();
    }

    public abstract void receiveMsg(Message msg);

    public boolean doesAllowDuplicate() { return allowDuplicate; }
    public boolean isFull() { return players.size() >= maxPlayers; }

    public GameData getData(Player player) { return datas.get(player); }

    public boolean isPlayer(User user) { return players.stream()
            .filter(p -> p.getUser().equals(user)).findAny().isPresent(); }

    public String getName() { return name; }
    public String getDescription() { return description; }

}
