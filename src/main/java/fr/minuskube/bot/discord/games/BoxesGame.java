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

public class BoxesGame extends Game {

    private static final Logger LOGGER = LoggerFactory.getLogger(BoxesGame.class);
    private static final int SIZE = 4;
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

    public BoxesGame() {
        super("boxes", "Two players take turns adding a single horizontal or vertical line " +
                "between two dots.");

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

            BoxesGameData data = new BoxesGameData(channel, player, opponent);

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
                    !(datas.get(p) instanceof BoxesGameData))
                return;

            BoxesGameData data = (BoxesGameData) datas.get(p);

            if(!data.isTurn(p))
                continue;

            char[] input = parseInput(msg.getContentDisplay());

            if(input == null) {
                channel.sendMessage(new MessageBuilder()
                        .append("Wrong format! Valid format: **A-B**", MessageBuilder.Formatting.ITALICS).build())
                        .queue(msg_ -> Executors.newScheduledThreadPool(1)
                                .schedule((Runnable) msg_.delete()::queue, 5, TimeUnit.SECONDS));
                return;
            }

            int first = ((int) input[0]) - ((int) 'A');
            int last = ((int) input[1]) - ((int) 'A');

            int dh = Math.abs((first % SIZE) - (last % SIZE));
            int dv = Math.abs((first / SIZE) - (last / SIZE));

            LOGGER.debug(dh + " / " + dv);

            if((dh != 1 || dv != 0) &&
                    (dh != 0 || dv != 1)) {

                channel.sendMessage(new MessageBuilder()
                        .append("Invalid placement...").build())
                        .queue();
                return;
            }

            int lh = Math.min(first, last) - (Math.min(first, last) / SIZE);
            int lv = Math.min(first, last);

            if(dh == 1 && data.getHLines()[lh] != -1) {
                channel.sendMessage(new MessageBuilder()
                        .append("There is already a line here...").build())
                        .queue();
                return;
            }
            else if(dv == 1 && data.getVLines()[lv] != -1) {
                channel.sendMessage(new MessageBuilder()
                        .append("There is already a line here...").build())
                        .queue();
                return;
            }

            if(dh == 1)
                data.setHLine(lh, data.getIndex(p));
            else
                data.setVLine(lv, data.getIndex(p));

            LOGGER.debug(Arrays.toString(data.getHLines()));
            LOGGER.debug(Arrays.toString(data.getVLines()));

            int[] squares = dh == 1 ? data.checkHSquares(lh) : data.checkVSquares(lv);
            int squareAmt = (squares[0] == -1 ? 0 : 1) + (squares[1] == -1 ? 0 : 1);

            if(squares[0] != -1)
                data.setSquare(squares[0], data.getIndex(p));
            if(squares[1] != -1)
                data.setSquare(squares[1], data.getIndex(p));

            data.addScore(p, squareAmt);

            if(squareAmt == 0)
                data.setTurn(data.opponent(p));

            sendImage(p, channel, data);

            if(checkFull(data)) {
                int p1Score = data.getScore(data.getPlayer(0));
                int p2Score = data.getScore(data.getPlayer(1));

                if(p1Score == p2Score) {
                    channel.sendMessage(new MessageBuilder()
                            .append("The game has ended! ")
                            .append("Nobody won!", MessageBuilder.Formatting.BOLD).build())
                            .queue();

                    end(p, channel);
                }
                else {
                    Player winner = p1Score > p2Score ? data.getPlayer(0) : data.getPlayer(1);

                    channel.sendMessage(new MessageBuilder()
                            .append("The game has ended! ")
                            .append("THE WINNER IS: ", MessageBuilder.Formatting.BOLD)
                            .append(winner.getMember().getUser()).build())
                            .queue();

                    end(p, channel);
                }
            }

            if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                msg.delete().queue();

            return;
        }
    }

    private char[] parseInput(String input) {
        if(input.length() != 3)
            return null;
        if(input.charAt(1) != '-')
            return null;

        String allowed = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".substring(0, SIZE * SIZE);
        input = input.toUpperCase();

        char first = input.charAt(0);
        char last = input.charAt(2);

        if(allowed.indexOf(first) == -1 || allowed.indexOf(last) == -1)
            return null;

        return new char[] { first, last };
    }


    private boolean checkFull(BoxesGameData data) {
        for(byte i : data.getSquares()) {
            if(i == -1)
                return false;
        }

        return true;
    }

    private void sendImage(Player player, TextChannel channel, BoxesGameData data) {
        if(data.getLastMsg() != null)
            data.getLastMsg().delete().queue();

        Member member = player.getMember();
        String userName = member.getEffectiveName();

        Member opponent = data.opponent(player).getMember();
        String opName = opponent.getEffectiveName();

        BufferedImage image = new BufferedImage(400, 400, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        // Draw Background
        {
            g2d.setColor(new Color(209, 209, 209));
            g2d.fillRoundRect(2, 2, 400 - 4, 400 - 4, 60, 60);

            g2d.setColor(new Color(196, 109, 39));
            g2d.setStroke(new BasicStroke(8));
            g2d.drawRoundRect(4, 4, 400 - 8, 400 - 8, 60, 60);
        }

        // Draw Squares, Circles and Lines
        {
            final int minDist = 30;
            final int size = 10 * (8 - SIZE);
            final int lineWidth = size / 3;
            final int dist = (400 - (minDist * 2) - (size * SIZE)) / (SIZE - 1);

            // Squares
            for(int x = 0; x < SIZE - 1; x++) {
                for(int y = 0; y < SIZE - 1; y++) {
                    int xx = minDist + (size / 2 * (x + 1)) + ((dist + (size / 2)) * x);
                    int yy = minDist + (size / 2 * (y + 1)) + ((dist + (size / 2)) * y);

                    int i = x + (y * (SIZE - 1));

                    if(data.getSquares()[i] == -1)
                        continue;
                    else if(data.getSquares()[i] == 0)
                        g2d.setColor(new Color(196, 76, 76, 80));
                    else
                        g2d.setColor(new Color(78, 144, 172, 80));

                    g2d.fillRect(xx, yy, dist + size, dist + size);
                }
            }

            // Horizontal Lines
            for(int x = 0; x < SIZE - 1; x++) {
                for(int y = 0; y < SIZE; y++) {
                    int xx = minDist + (size * (x + 1)) + (dist * x);
                    int yy = minDist + ((dist + size) * y) + ((size - lineWidth) / 2);

                    int i = x + (y * (SIZE - 1));

                    if(data.getHLines()[i] == -1)
                        g2d.setColor(new Color(184, 184, 184));
                    else if(data.getHLines()[i] == 0)
                        g2d.setColor(new Color(196, 76, 76));
                    else
                        g2d.setColor(new Color(78, 144, 172));

                    g2d.fillRect(xx - 2, yy, dist + 4, lineWidth);
                }
            }

            // Vertical Lines
            for(int x = 0; x < SIZE; x++) {
                for(int y = 0; y < SIZE - 1; y++) {
                    int xx = minDist + ((dist + size) * x) + ((size - lineWidth) / 2);
                    int yy = minDist + (size * (y + 1)) + (dist * y);

                    int i = x + (y * SIZE);

                    if(data.getVLines()[i] == -1)
                        g2d.setColor(new Color(184, 184, 184));
                    else if(data.getVLines()[i] == 0)
                        g2d.setColor(new Color(196, 76, 76));
                    else
                        g2d.setColor(new Color(78, 144, 172));

                    g2d.fillRect(xx, yy - 2, lineWidth, dist + 4);
                }
            }

            // Circles3
            g2d.setFont(font.deriveFont(size / 1.5f));

            for(int x = 0; x < SIZE; x++) {
                for(int y = 0; y < SIZE; y++) {
                    int xx = minDist + ((dist + size) * x);
                    int yy = minDist + ((dist + size) * y);

                    g2d.setColor(new Color(196, 109, 39));
                    g2d.fillOval(xx, yy, size, size);

                    // Letters
                    String letter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".charAt(x + (y * SIZE)) + "";
                    FontMetrics metrics = g2d.getFontMetrics();

                    LOGGER.debug(letter);

                    g2d.setColor(Color.WHITE);
                    g2d.drawString(letter, xx + ((size - metrics.stringWidth(letter)) / 2),
                            yy + ((size - metrics.getHeight()) / 2) + metrics.getAscent());
                }
            }
        }

        try {
            File tempFile = StreamUtils.tempFileFromImage(image, "game-boxes", ".png");

            Message msg = channel.sendMessage(new MessageBuilder()
                    .append("Turn: ", MessageBuilder.Formatting.BOLD)
                    .append(data.getTurn().getMember().getUser()).build())
                    .addFile(tempFile).complete();

            data.setLastMsg(msg);
        } catch(IOException e) {
            LOGGER.error("Couldn't send image: ", e);
        }
    }

    @Override
    public void end(Player player, TextChannel channel) {
        if(datas.containsKey(player)) {
            Player opponent = ((BoxesGameData) datas.get(player)).opponent(player);
            super.end(opponent, channel);

            datas.remove(player);
            datas.remove(opponent);
        }

        super.end(player, channel);

        if(waitingPlayers.get(channel) == player)
            waitingPlayers.remove(channel);
    }

    class BoxesGameData extends GameData {

        private Message lastMsg;

        private Player turn;
        private byte[] hLines = new byte[SIZE * (SIZE - 1)];
        private byte[] vLines = new byte[SIZE * (SIZE - 1)];
        private byte[] squares = new byte[(SIZE - 1) * (SIZE - 1)];

        private int[] scores = new int[2];

        public BoxesGameData(TextChannel channel, Player player1, Player player2) {
            super(channel, player1, player2);

            this.turn = new Random().nextBoolean() ? player1 : player2;
            Arrays.fill(squares, (byte) -1);
            Arrays.fill(hLines, (byte) -1);
            Arrays.fill(vLines, (byte) -1);
        }

        public int[] checkHSquares(int lh) {
            int[] squares = new int[2];
            Arrays.fill(squares, -1);

            if(lh >= SIZE - 1) {
                int upV = lh + (lh / (SIZE - 1)) - SIZE;
                int upH = lh - (SIZE - 1);

                if(getVLines()[upV] != -1 &&
                        getVLines()[upV + 1] != -1 &&
                        getHLines()[upH] != -1)
                    squares[0] = upH;
            }
            if(lh <= (SIZE * (SIZE - 1)) - SIZE) {
                int downV = lh + (lh / (SIZE - 1));
                int downH = lh + (SIZE - 1);

                if(getVLines()[downV] != -1 &&
                        getVLines()[downV + 1] != -1 &&
                        getHLines()[downH] != -1)
                    squares[1] = lh;
            }

            return squares;
        }

        public int[] checkVSquares(int lv) {
            int[] squares = new int[2];
            Arrays.fill(squares, -1);

            if(lv % SIZE != 0) {
                int leftH = lv - (lv / SIZE) - 1;
                int leftV = lv - 1;

                if(getHLines()[leftH] != -1 &&
                        getHLines()[leftH + (SIZE - 1)] != -1 &&
                        getVLines()[leftV] != -1)
                    squares[0] = leftH;
            }
            if((lv - (SIZE - 1)) % SIZE != 0) {
                int rightH = lv - (lv / SIZE);
                int rightV = lv + 1;

                if(getHLines()[rightH] != -1 &&
                        getHLines()[rightH + (SIZE - 1)] != -1 &&
                        getVLines()[rightV] != -1)
                    squares[1] = rightH;
            }

            return squares;
        }

        public byte getIndex(Player player) {
            for(int i = 0; i < players.length; i++)
                if(players[i] == player)
                    return (byte) i;

            return -1;
        }

        public int getScore(Player player) {
            for(int i = 0; i < players.length; i++) {
                if(players[i] == player)
                    return scores[i];
            }

            return 0;
        }
        public void addScore(Player player, int amount) {
            for(int i = 0; i < players.length; i++) {
                if(players[i] == player) {
                    scores[i] += amount;
                    return;
                }
            }
        }

        public byte[] getSquares() { return squares; }
        public byte getSquare(int index) { return squares[index]; }
        public void setSquare(int index, byte value) { this.squares[index] = value; }

        public Message getLastMsg() { return lastMsg; }
        public void setLastMsg(Message lastMsg) { this.lastMsg = lastMsg; }

        public Player getTurn() { return turn; }
        public boolean isTurn(Player player) { return turn.equals(player); }
        public void setTurn(Player player) { this.turn = player; }

        public Player opponent(Player player) { return player != players[0] ? players[0] : players[1]; }

        public byte[] getHLines() { return hLines; }
        public void setHLine(int index, byte value) { this.hLines[index] = value; }

        public byte[] getVLines() { return vLines; }
        public void setVLine(int index, byte value) { this.vLines[index] = value; }

    }

}
