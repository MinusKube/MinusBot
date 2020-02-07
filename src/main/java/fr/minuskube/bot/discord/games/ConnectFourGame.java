package fr.minuskube.bot.discord.games;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.StreamUtils;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Channel;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConnectFourGame extends Game {

    private static final Logger LOGGER = LoggerFactory.getLogger(ConnectFourGame.class);
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

    private Map<Channel, Player> waitingPlayers = new HashMap<>();

    public ConnectFourGame() {
        super("c4", "(Connect Four) You have to connect four discs of the same color next to each other.");

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

            C4GameData data = new C4GameData(channel, player, opponent);

            datas.put(player, data);
            datas.put(opponent, data);

            sendImage(player, channel, data);
        }
    }

    @Override
    public void receiveMsg(Message msg) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member author = guild.getMember(msg.getAuthor());

        List<Player> players = Player.getPlayers(author);

        for(Player p : players) {
            if(datas.get(p) == null ||
                    !(datas.get(p) instanceof C4GameData))
                return;

            C4GameData data = (C4GameData) datas.get(p);

            if(!data.isTurn(p))
                continue;

            try {
                int input = Integer.parseInt(msg.getContent());

                if(input < 1 || input > 7) {
                    channel.sendMessage(new MessageBuilder()
                            .append("Please enter a number between 1 and 7...").build())
                            .queue();
                    return;
                }

                if(data.getGrid()[input - 1][0] != -1) {
                    channel.sendMessage(new MessageBuilder()
                            .append("This column is full!").build())
                            .queue();
                    return;
                }

                int last = data.getGrid()[input - 1].length;

                for(int y = 0; y < data.getGrid()[input - 1].length; y++) {
                    if(data.getGrid()[input - 1][y] != -1) {
                        last = y;
                        break;
                    }
                }

                data.setGridSpace(input - 1, last - 1, data.getColor(p));
                data.setTurn(data.opponent(p));

                data.setLastX(input - 1);
                data.setLastY(last - 1);

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

    private void sendImage(Player player, TextChannel channel, C4GameData data) {
        if(data.getLastMsg() != null)
            data.getLastMsg().delete().queue();

        Member member = player.getMember();
        String userName = member.getEffectiveName();

        Member opponent = data.opponent(player).getMember();
        String opName = opponent.getEffectiveName();

        BufferedImage image = new BufferedImage(670, 420, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        g2d.setColor(new Color(45, 92, 250));
        g2d.fillRect(0, 35, 510, 420);

        g2d.setColor(new Color(25, 62, 200));
        g2d.fillRect(0, 0, 510, 35);

        for(int x = 0; x < data.getGrid().length; x++) {
            int xx = 20 + (70 * x);

            g2d.setFont(font.deriveFont(26f));

            String id = (x + 1) + "";

            g2d.setColor(new Color(100, 100, 100));
            g2d.drawString(id, xx + 25 - (g2d.getFontMetrics().stringWidth(id) / 2) + 1,
                    27 + 1);

            g2d.setColor(new Color(200, 200, 200));
            g2d.drawString(id, xx + 25 - (g2d.getFontMetrics().stringWidth(id) / 2),
                    27);

            if(x > 0) {
                g2d.setColor(new Color(0, 0, 0, 50));
                g2d.setStroke(new BasicStroke(2));
                g2d.drawLine(xx - 10, 0, xx - 10, 420);
            }

            for(int y = 0; y < data.getGrid()[x].length; y++) {
                int yy = 50 + (60 * y);

                byte disc = data.getGrid()[x][y];

                Color color;
                Color darkColor;

                switch(disc) {
                    case -1:
                    default:
                        color = new Color(15, 62, 210);
                        break;
                    case 0:
                        color = new Color(224, 224, 56);
                        break;
                    case 1:
                        color = new Color(224, 56, 56);
                        break;
                }

                if(x == data.getLastX() && y == data.getLastY()) {
                    g2d.setColor(new Color(50, 50, 255, 120));
                    g2d.fillRect(xx - 10, yy - 5, 70, 61);
                }

                g2d.setColor(color.darker());
                g2d.fillOval(xx + 1, yy + 2, 50, 50);

                g2d.setColor(color);
                g2d.fillOval(xx, yy, 50, 50);
            }
        }

        g2d.setFont(fontBold.deriveFont(20f));

        g2d.setColor(new Color(224, 224, 56));
        g2d.fillOval(515, 30, 25, 25);
        g2d.drawString((data.getColor(player) == 0 ? userName : opName), 545, 48);

        g2d.setColor(new Color(224, 56, 56));
        g2d.fillOval(515, 60, 25, 25);
        g2d.drawString((data.getColor(player) == 1 ? userName : opName), 545, 78);

        try {
            File tempFile = StreamUtils.tempFileFromImage(image, "game-c4", ".png");

            Message msg = channel.sendFile(tempFile, new MessageBuilder()
                    .append("Turn: ", MessageBuilder.Formatting.BOLD)
                    .append(data.getTurn().getMember().getUser()).build()).complete();

            data.setLastMsg(msg);
        } catch(IOException e) {
            LOGGER.error("Couldn't send image:", e);
        }
    }

    private boolean checkWin(C4GameData data, Player p) {
        int score = 0;

        int xl = data.getGrid().length;
        int yl = data.getGrid()[0].length;

        // Horizontal Check
        for(int x = 0; x < xl; x++) {
            for(int y = 0; y < yl; y++) {
                if(data.getGrid()[x][y] == data.getColor(p)) {
                    score++;

                    if(score >= 4)
                        return true;
                }
                else
                    score = 0;
            }

            score = 0;
        }

        // Vertical Check
        for(int y = 0; y < yl; y++) {
            for(int x = 0; x < xl; x++) {
                if(data.getGrid()[x][y] == data.getColor(p)) {
                    score++;

                    if(score >= 4)
                        return true;
                }
                else
                    score = 0;
            }

            score = 0;
        }

        // Diagonal Right Check
        for(int x = 0; x < xl - 3; x++) {
            for(int y = 0; y < yl - 3; y++) {
                for(int i = 0; i < 4; i++) {
                    if(data.getGrid()[x + i][y + i] == data.getColor(p)) {
                        score++;

                        if(score >= 4)
                            return true;
                    }
                    else
                        score = 0;
                }

                score = 0;
            }
        }

        // Diagonal Left Check
        for(int x = 3; x < xl; x++) {
            for(int y = 0; y < yl - 3; y++) {
                for(int i = 0; i < 4; i++) {
                    if(data.getGrid()[x - i][y + i] == data.getColor(p)) {
                        score++;

                        if(score >= 4)
                            return true;
                    }
                    else
                        score = 0;
                }

                score = 0;
            }
        }

        return false;
    }

    private boolean checkFull(C4GameData data) {
        for(byte[] x : data.getGrid()) {
            if(x[0] == -1)
                return false;
        }

        return true;
    }

    @Override
    public void end(Player player, TextChannel channel) {
        if(datas.containsKey(player)) {
            Player opponent = ((C4GameData) datas.get(player)).opponent(player);
            super.end(opponent, channel);

            datas.remove(player);
            datas.remove(opponent);
        }

        super.end(player, channel);

        if(waitingPlayers.get(channel) == player)
            waitingPlayers.remove(channel);
    }

    class C4GameData extends GameData {

        private Message lastMsg;

        private Player turn;
        private byte[][] grid = new byte[7][6];

        private int lastX = -1;
        private int lastY = -1;

        public C4GameData(TextChannel channel, Player player1, Player player2) {
            super(channel, player1, player2);

            this.turn = new Random().nextBoolean() ? player1 : player2;

            for(byte[] aGrid : grid)
                Arrays.fill(aGrid, (byte) -1);
        }

        public byte getColor(Player player) {
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

        public byte[][] getGrid() { return grid; }
        public void setGridSpace(int row, int column, byte value) { grid[row][column] = value; }

        public int getLastX() { return lastX; }
        public void setLastX(int lastX) { this.lastX = lastX; }

        public int getLastY() { return lastY; }
        public void setLastY(int lastY) { this.lastY = lastY; }

    }

}
