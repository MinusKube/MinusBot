package fr.minuskube.bot.discord.games;

import net.dv8tion.jda.entities.User;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {

    private static List<Player> players = new ArrayList<Player>();

    private User user;

    public Player(User user) {
        this.user = user;

        players.add(this);
    }

    public void delete() { players.remove(this); }
    public User getUser() { return user; }

    public static List<Player> getPlayers(User user) {
        return players.stream()
            .filter(p -> p.getUser().equals(user))
            .collect(Collectors.toList());
    }

}
