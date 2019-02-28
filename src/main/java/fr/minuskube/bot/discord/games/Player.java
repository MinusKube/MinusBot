package fr.minuskube.bot.discord.games;

import net.dv8tion.jda.core.entities.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Player {

    private static List<Player> players = new ArrayList<Player>();

    private final Member member;

    public Player(Member member) {
        this.member = member;

        players.add(this);
    }

    public void delete() { players.remove(this); }
    public Member getMember() { return member; }

    public static List<Player> getPlayers(Member member) {
        return players.stream()
            .filter(p -> p.getMember().equals(member))
            .collect(Collectors.toList());
    }

}
