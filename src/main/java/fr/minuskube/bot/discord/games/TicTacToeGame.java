package fr.minuskube.bot.discord.games;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.StreamUtils;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class TicTacToeGame extends Game {

    private static final Logger LOGGER = LoggerFactory.getLogger(TicTacToeGame.class);
    private static Font font, fontBold;

    static {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT,
                    DiscordBot.class.getResourceAsStream("/fonts/PTSans.ttf"));
            fontBold = Font.createFont(Font.TRUETYPE_FONT,
                    DiscordBot.class.getResourceAsStream("/fonts/PTSans_.ttf"));
        } catch(FontFormatException | IOException e) {
            font = fontBold = new Font("Arial", Font.PLAIN, 20);
            LOGGER.error("Couldn't init fonts:", e);
        }
    }

    private Map<TextChannel, Player> waitingPlayers = new HashMap<>();

    public TicTacToeGame() {
        super("tictactoe", "Two players, X and O, take turns marking the spaces in a 3×3 grid.");

        this.allowDuplicate = true;
        this.maxPlayers = 2;
    }

    @Override
    public void start(Player player, TextChannel channel) {
        super.start(player, channel);

        if(!waitingPlayers.containsKey(channel)) {
            waitingPlayers.put(channel, player);

            channel.sendMessage(new MessageBuilder()
                    .append("Game joined! The game will start when another player will join.").build())
                    .queue();
        }
        else {
            Player opponent = waitingPlayers.get(channel);
            waitingPlayers.remove(channel);

            TTTGameData data = new TTTGameData(channel, player, opponent);

            datas.put(player, data);
            datas.put(opponent, data);

            sendImage(player, channel, data);
        }
    }

    @Override
    public void receiveMsg(Message msg) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member author = guild.retrieveMember(msg.getAuthor()).complete();

        List<Player> players = Player.getPlayers(author);

        for(Player p : players) {
            if(datas.get(p) == null ||
                    !(datas.get(p) instanceof TTTGameData))
                return;

            TTTGameData data = (TTTGameData) datas.get(p);

            if(!data.isTurn(p))
                continue;

            try {
                int input = Integer.parseInt(msg.getContentDisplay());

                if(input < 1 || input > 9) {
                    channel.sendMessage(new MessageBuilder()
                            .append("Please enter a number between 1 and 9...").build())
                            .queue();
                    return;
                }

                if(data.getGrid()[input - 1] != -1) {
                    channel.sendMessage(new MessageBuilder()
                            .append("There is already a shape on this space...").build())
                            .queue();
                    return;
                }

                data.setGridSpace(input - 1, data.getShape(p));
                data.setTurn(data.opponent(p));

                sendImage(p, channel, data);

                Player winner = null;

                if(checkWin(data, p))
                    winner = p;
                if(checkWin(data, data.opponent(p)))
                    winner = data.opponent(p);

                if(winner != null) {
                    channel.sendMessage(new MessageBuilder()
                            .append("The game has ended! ")
                            .append("THE WINNER IS: ", MessageBuilder.Formatting.BOLD)
                            .append(winner.getMember().getUser()).build())
                            .queue();

                    end(p, channel);
                }
                else if(checkFull(data)) {
                    channel.sendMessage(new MessageBuilder()
                            .append("The game has ended! ")
                            .append("Nobody won!", MessageBuilder.Formatting.BOLD).build())
                            .queue();

                    end(p, channel);
                }

                if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                    msg.delete().queue();
            } catch(NumberFormatException e) {
                channel.sendMessage(new MessageBuilder()
                        .append("Sorry, this is not a number...", MessageBuilder.Formatting.ITALICS).build())
                        .queue(msg_ -> Executors.newScheduledThreadPool(1)
                                .schedule((Runnable) msg_.delete()::queue, 5, TimeUnit.SECONDS));
            }

            return;
        }
    }

    private boolean checkWin(TTTGameData data, Player p) {
        int score = 0;

        for(int x = 0; x < 3; x++) {
            for(int y = 0; y < 3; y++) {
                if(data.getGrid()[x % 3 + y * 3] == data.getShape(p)) {
                    score++;

                    if(score >= 3)
                        return true;
                }
            }

            score = 0;
        }

        for(int y = 0; y < 3; y++) {
            for(int x = 0; x < 3; x++) {
                if(data.getGrid()[x % 3 + y * 3] == data.getShape(p)) {
                    score++;

                    if(score >= 3)
                        return true;
                }
            }

            score = 0;
        }

        for(int x = 0; x < 3; x++) {
            for(int y = 0; y < 3; y++) {
                if(x != y)
                    continue;

                if(data.getGrid()[x % 3 + y * 3] == data.getShape(p)) {
                    score++;

                    if(score >= 3)
                        return true;
                }
            }
        }

        score = 0;

        for(int x = 2; x >= 0; x--) {
            for(int y = 2; y >= 0; y--) {
                if(x != y)
                    continue;

                if(data.getGrid()[x % 3 + y * 3] == data.getShape(p)) {
                    score++;

                    if(score >= 3)
                        return true;
                }
            }
        }

        return false;
    }

    private boolean checkFull(TTTGameData data) {
        for(byte i : data.getGrid()) {
            if(i == -1)
                return false;
        }

        return true;
    }

    private void sendImage(Player player, TextChannel channel, TTTGameData data) {
        if(data.getLastMsg() != null)
            data.getLastMsg().delete().queue();

        Member member = player.getMember();
        String userName = member.getEffectiveName();

        Member opponent = data.opponent(player).getMember();
        String opName = opponent.getEffectiveName();

        BufferedImage image = new BufferedImage(350, 162, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(162, 100, 40));

        g2d.fillRect(0, 0, 162, 3);
        g2d.fillRect(0, 0, 3, 162);
        g2d.fillRect(162 - 3, 0, 3, 162);
        g2d.fillRect(0, 162 - 3, 162, 3);

        g2d.fillRect(0, 53, 162, 3);
        g2d.fillRect(0, 100 + 6, 162, 3);
        g2d.fillRect(53, 0, 3, 162);
        g2d.fillRect(100 + 6, 0, 3, 162);

        g2d.setFont(font.deriveFont(20f));

        for(int i = 0; i < 9; i++) {
            int x = 50 * (i % 3) + (3 * (i % 3) + 3),
                    y = 50 * (i / 3) + (3 * (i / 3) + 3);

            if(data.getGrid()[i] == (byte) 0) {
                g2d.setColor(new Color(140, 40, 40));

                g2d.setStroke(new BasicStroke(4));
                g2d.drawLine(x + 14, y + 14, x + 36, y + 36);
                g2d.drawLine(x + 36, y + 14, x + 14, y + 36);
            }
            else if(data.getGrid()[i] == (byte) 1) {
                g2d.setColor(new Color(30, 30, 100));

                g2d.setStroke(new BasicStroke(3.5f));
                g2d.drawOval(x + 8, y + 9, 32, 32);
            }
            else {
                String txt = (i + 1) + "";

                g2d.setColor(new Color(162, 100, 40));
                g2d.drawString(txt, x + 25 - (g2d.getFontMetrics().stringWidth(txt) / 2),
                        y + 25 + 7);
            }
        }

        g2d.setFont(fontBold.deriveFont(17f));

        g2d.setColor(new Color(140, 40, 40));
        g2d.drawString("X = " + (data.getShape(player) == 0 ? userName : opName), 180, 40);

        g2d.setColor(new Color(30, 30, 100));
        g2d.drawString("O = " + (data.getShape(player) == 1 ? userName : opName), 180, 58);

        g2d.setColor(new Color(100, 100, 100));
        g2d.drawString("Turn: " + data.getTurn().getMember().getEffectiveName(), 180, 100);

        try {
            File tempFile = StreamUtils.tempFileFromImage(image, "game-ttt", ".png");
            Message msg = channel.sendFile(tempFile).complete();

            data.setLastMsg(msg);
        } catch(IOException e) {
            LOGGER.error("Couldn't send image: ", e);
        }
    }

    @Override
    public void end(Player player, TextChannel channel) {
        if(datas.containsKey(player)) {
            Player opponent = ((TTTGameData) datas.get(player)).opponent(player);
            super.end(opponent, channel);

            datas.remove(player);
            datas.remove(opponent);
        }

        super.end(player, channel);

        if(waitingPlayers.get(channel) == player)
            waitingPlayers.remove(channel);
    }

    class TTTGameData extends GameData {

        private Message lastMsg;

        private Player turn;
        private byte[] grid = new byte[9];

        public TTTGameData(TextChannel channel, Player player1, Player player2) {
            super(channel, player1, player2);

            this.turn = new Random().nextBoolean() ? player1 : player2;
            Arrays.fill(grid, (byte) -1);
        }

        public byte getShape(Player player) {
            for(int i = 0; i < players.length; i++)
                if(players[i] == player)
                    return (byte) i;

            return -1;
        }

        public Message getLastMsg() { return lastMsg; }
        public void setLastMsg(Message lastMsg) { this.lastMsg = lastMsg; }

        public Player getTurn() { return turn; }
        public boolean isTurn(Player player) { return turn.equals(player); }
        public void setTurn(Player player) { this.turn = player; }

        public Player opponent(Player player) { return player != players[0] ? players[0] : players[1]; }

        public byte[] getGrid() { return grid; }
        public void setGridSpace(int index, byte value) { grid[index] = value; }

    }

}
