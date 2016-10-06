package fr.minuskube.bot.discord.games;

import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NumberGame extends Game {

    private static Random random = new Random();
    private static final short TRIES = 6;
    private static final int MAX = 100;

    public NumberGame() {
        super("number", "Find a number randomly generated between 1 and " + MAX + ".");
    }

    @Override
    public void start(Player player, TextChannel channel) {
        super.start(player, channel);

        datas.put(player, new NumberGameData(channel, player, random.nextInt(MAX) + 1));

        channel.sendMessage(new MessageBuilder()
                .appendString("Welcome to the Find The Number game.\n")
                .appendString("Find the correct number between 1 and " + MAX + ", ")
                .appendString("you have `" + TRIES + "` tries.").build());
    }

    @Override
    public void receiveMsg(Message msg) {
        Player p = Player.getPlayers(msg.getAuthor()).get(0);
        NumberGameData data = ((NumberGameData) datas.get(p));

        try {
            int input = Integer.parseInt(msg.getContent());

            if(input < 1 || input > MAX) {
                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString("Please enter a number between 1 and " + MAX + "...").build());
                return;
            }

            if(((TextChannel) msg.getChannel()).checkPermission(DiscordBotAPI.self(), Permission.MESSAGE_MANAGE))
                msg.deleteMessage();

            if(data.getLastMsg() != null)
                data.getLastMsg().deleteMessage();

            data.addTry(input);

            int number = data.getNumber();
            int triesLeft = data.getTriesLeft() - 1;

            if(input == number) {
                String triesStr = data.getTries().toString();

                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString("\n`Tries: " + triesStr + "`\n\n")
                        .appendString("Yeah, correct guess (**`" + number + "`**), you won with `" + triesLeft + "`"
                                + (triesLeft < 2 ? " try" : " tries") + " left.\n")
                        .appendString("Thanks for playing!").build());

                end(p, (TextChannel) msg.getChannel());
                return;
            }

            if(triesLeft > 0) {
                String triesStr = data.getTries().toString();

                Message msg_ = msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString("\n`Tries: " + triesStr + "`\n\n")
                        .appendString("Wrong guess! The correct number is **" + (number < input ? "lower" : "higher")
                                + "** than " + input + ".\n")
                        .appendString("`" + triesLeft + "`" + (triesLeft < 2 ? " try" : " tries") + " left.").build());

                data.setLastMsg(msg_);
            }
            else {
                String triesStr = data.getTries().toString();

                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString("\n`Tries: " + triesStr + "`\n\n")
                        .appendString("Wrong guess! You lose!\n", MessageBuilder.Formatting.BOLD)
                        .appendString("The correct number was " + number + ".").build());

                end(p, (TextChannel) msg.getChannel());
                return;
            }

            data.setTriesLeft(triesLeft);
        } catch(NumberFormatException e) {
            Message msg_ = msg.getChannel().sendMessage(new MessageBuilder()
                    .appendString("Sorry, this is not a number...", MessageBuilder.Formatting.ITALICS).build());

            Executors.newScheduledThreadPool(1).schedule(msg_::deleteMessage, 5, TimeUnit.SECONDS);
        }
    }

    @Override
    public void end(Player player, TextChannel channel) {
        super.end(player, channel);

        datas.remove(player);
    }

    class NumberGameData extends GameData {

        private Message lastMsg;

        private List<Integer> tries = new ArrayList<>();

        private int number;
        private int triesLeft;

        public NumberGameData(TextChannel channel, Player player, int number) {
            super(channel, player);

            this.number = number;
            this.triesLeft = TRIES;
        }

        public Message getLastMsg() { return lastMsg; }
        public void setLastMsg(Message lastMsg) { this.lastMsg = lastMsg; }

        public int getNumber() { return number; }

        public List<Integer> getTries() { return tries; }
        public void addTry(int try_) { tries.add(try_); }

        public int getTriesLeft() { return triesLeft; }
        public void setTriesLeft(int triesLeft) { this.triesLeft = triesLeft; }

    }

}
