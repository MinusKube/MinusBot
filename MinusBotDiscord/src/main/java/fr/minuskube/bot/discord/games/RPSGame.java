package fr.minuskube.bot.discord.games;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.StreamUtils;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import net.dv8tion.jda.entities.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class RPSGame extends Game {

    private static final Logger LOGGER = LoggerFactory.getLogger(RPSGame.class);

    private static final short MAX_ROUND = 3;
    private static final String[] IMG_NAMES = { "rock.png", "paper.png", "scissors.png" };

    private static Random random = new Random();
    private static Font font;

    static {
        try {
            font = Font.createFont(Font.TRUETYPE_FONT,
                    DiscordBot.class.getResourceAsStream("/fonts/Bebas.ttf"));
        } catch(FontFormatException | IOException e) {
            font = new Font("Arial", Font.PLAIN, 20);
            LOGGER.error("Couldn't init fonts:", e);
        }
    }

    public RPSGame() {
        super("rps", "Rock, Paper and Scissors game!");
    }

    @Override
    public void start(Player player, TextChannel channel) {
        super.start(player, channel);

        datas.put(player, new RPSGameData(channel, player));

        try {
            File tempFile = StreamUtils.tempFileFromInputStream(DiscordBot.class.getResourceAsStream("/imgs/rps.png"),
                    "game-rps", ".png");
            channel.sendFile(tempFile, new MessageBuilder().appendString("Choose one!").build());
        } catch(IOException e) {
            LOGGER.error("Couldn't send image:", e);
        }
    }

    @Override
    public void receiveMsg(Message msg) {
        Player p = Player.getPlayers(msg.getAuthor()).get(0);
        RPSGameData data = ((RPSGameData) datas.get(p));

        byte choice;

        if(msg.getContent().equalsIgnoreCase("rock"))
            choice = 0;
        else if(msg.getContent().equalsIgnoreCase("paper"))
            choice = 1;
        else if(msg.getContent().equalsIgnoreCase("scissors") || msg.getContent().equalsIgnoreCase("scissor"))
            choice = 2;
        else {
            Message msg_ = msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Please, type `Rock`, `Paper` or `Scissors`...", MessageBuilder.Formatting.ITALICS)
                    .build());

            Executors.newScheduledThreadPool(1).schedule(msg_::deleteMessage, 5, TimeUnit.SECONDS);
            return;
        }

        data.setRound(data.getRound() + 1);

        byte aiChoice = (byte) random.nextInt(3);

        data.setChoice(choice);
        data.setAiChoice(aiChoice);

        byte winner = getWinner(choice, aiChoice);

        if(winner == 0)
            data.addPlayerPts(1);
        else if(winner == 1)
            data.addAiPts(1);

        if(((TextChannel) msg.getChannel()).checkPermission(DiscordBotAPI.self(), Permission.MESSAGE_MANAGE))
            msg.deleteMessage();

        sendImage(p, (TextChannel) msg.getChannel(), data, winner);
    }

    private void sendImage(Player player, TextChannel channel, RPSGameData data, byte winner) {
        if(data.getLastMsg() != null)
            data.getLastMsg().deleteMessage();

        User user = player.getUser();

        BufferedImage image = new BufferedImage(350, 150, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = image.createGraphics();
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);

        String score = data.getPlayerPts() + " - " + data.getAiPts();
        g2d.setFont(font.deriveFont(40f));

        g2d.setColor(Color.GRAY);
        g2d.fillRect((350 / 2) - (g2d.getFontMetrics().stringWidth(score) / 2) - 5, 0,
                g2d.getFontMetrics().stringWidth(score) + 10, 50);

        g2d.setColor(Color.BLACK);
        g2d.drawString(score, (350 / 2) - (g2d.getFontMetrics().stringWidth(score) / 2), 43);

        g2d.setColor(Color.GRAY);

        try {
            Image choiceImg = ImageIO.read(DiscordBot.class.getResourceAsStream("/imgs/"
                    + IMG_NAMES[data.getChoice()]));
            Image aiChoiceImg = ImageIO.read(DiscordBot.class.getResourceAsStream("/imgs/"
                    + IMG_NAMES[data.getAiChoice()]));

            g2d.drawImage(choiceImg, 5, 150 - 106, null);
            g2d.drawImage(aiChoiceImg, 345 - 100, 150 - 106, null);
        } catch(IOException e) {
            LOGGER.error("Couldn't load images:", e);
        }

        g2d.setFont(font.deriveFont(50f));
        g2d.drawString("VS", (350 / 2) - (g2d.getFontMetrics().stringWidth("VS") / 2),
                150 - (106 / 2) + 25);

        g2d.setFont(font.deriveFont(30f));

        if(winner == -1) {
            g2d.setColor(Color.GRAY);
            g2d.drawString("TIE", (350 / 2) - (g2d.getFontMetrics().stringWidth("TIE") / 2),
                    150 - (106 / 2) + 60);
        }
        else {
            String win = "WINNER";
            String lose = "LOSER";

            g2d.setColor(winner == 0 ? Color.GREEN : Color.RED);
            g2d.drawString(winner == 0 ? win : lose, 5 + (106 / 2)
                    - (g2d.getFontMetrics().stringWidth(winner == 0 ? win : lose) / 2),
                    50 - 10);

            g2d.setColor(winner == 1 ? Color.GREEN : Color.RED);
            g2d.drawString(winner == 1 ? win : lose, 345 - 100 + (106 / 2)
                            - (g2d.getFontMetrics().stringWidth(winner == 1 ? win : lose) / 2),
                    50 - 10);
        }

        try {
            File tempFile = StreamUtils.tempFileFromImage(image, "game-rps-" + user.getUsername().toLowerCase(),
                    ".png");

            Message msg = channel.sendFile(tempFile, null);
            data.setLastMsg(msg);
        } catch(IOException e) {
            LOGGER.error("Couldn't send image:", e);
        }
    }

    @Override
    public void end(Player player, TextChannel channel) {
        super.end(player, channel);

        datas.remove(player);
    }

    private byte getWinner(byte choice1, byte choice2) {
        if(choice1 == choice2)
            return -1;

        switch(choice1) {
            case 0:
                return (byte) (choice2 == 2 ? 0 : 1);
            case 1:
                return (byte) (choice2 == 0 ? 0 : 1);
            case 2:
                return (byte) (choice2 == 1 ? 0 : 1);
        }

        return -1;
    }

    class RPSGameData extends GameData {

        private Message lastMsg;

        private int round;

        private int playerPts;
        private int aiPts;

        private byte choice;
        private byte aiChoice;

        public RPSGameData(TextChannel channel, Player player) {
            super(channel, player);
        }

        public Message getLastMsg() { return lastMsg; }
        public void setLastMsg(Message lastMsg) { this.lastMsg = lastMsg; }

        public int getRound() { return round; }
        public void setRound(int round) { this.round = round; }

        public int getPlayerPts() { return playerPts; }
        public void addPlayerPts(int amount) { this.playerPts += amount; }

        public int getAiPts() { return aiPts; }
        public void addAiPts(int amount) { this.aiPts += amount; }

        public byte getChoice() { return choice; }
        public void setChoice(byte choice) { this.choice = choice; }

        public byte getAiChoice() { return aiChoice; }
        public void setAiChoice(byte aiChoice) { this.aiChoice = aiChoice; }

    }

}
