package fr.minuskube.bot.discord.games;

import net.dv8tion.jda.api.entities.TextChannel;

public abstract class GameData {

    protected final TextChannel channel;
    protected Player[] players;

    public GameData(TextChannel channel, Player... players) {
        this.channel = channel;
        this.players = players;
    }

    public TextChannel getChannel() { return channel; }

    public Player[] getPlayers() { return players; }
    public Player getPlayer(int index) { return players[index]; }
    public void setPlayers(Player[] players) { this.players = players; }
    public void setPlayer(int index, Player player) { this.players[index] = player; }

}
