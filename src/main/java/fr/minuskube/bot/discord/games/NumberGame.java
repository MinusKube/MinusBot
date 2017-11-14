package fr.minuskube.bot.discord.games;

import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class NumberGame extends Game {

    private static final Logger LOGGER = LoggerFactory.getLogger(NumberGame.class);

    private static final short TRIES = 6;
    private static final int MAX = 100;
    private static Random random = new Random();

    public NumberGame() {
        super("number", "Find a number randomly generated between 1 and " + MAX + ".");
    }

    @Override
    public void start(Player player, TextChannel channel) {
        super.start(player, channel);

        datas.put(player, new NumberGameData(channel, player, random.nextInt(MAX) + 1));

        channel.sendMessage(new MessageBuilder()
                .append("Welcome to the Find The Number game.\n")
                .append("Find the correct number between 1 and " + MAX + ", ")
                .append("you have `" + TRIES + "` tries.").build())
                .queue();
    }

    @Override
    public void receiveMsg(Message msg) {
        TextChannel channel = (TextChannel) msg.getChannel();
        Guild guild = channel.getGuild();
        Member author = guild.getMember(msg.getAuthor());

        Player p = Player.getPlayers(author).get(0);
        NumberGameData data = ((NumberGameData) datas.get(p));

        try {
            int input = Integer.parseInt(msg.getContent());

            if(input < 1 || input > MAX) {
                channel.sendMessage(new MessageBuilder()
                        .append("Please enter a number between 1 and " + MAX + "...").build())
                        .queue();
                return;
            }

            if(guild.getSelfMember().hasPermission(channel, Permission.MESSAGE_MANAGE))
                msg.delete().queue();

            if(data.getLastMsg() != null)
                data.getLastMsg().delete().queue();

            data.addTry(input);

            int number = data.getNumber();
            int triesLeft = data.getTriesLeft() - 1;

            if(input == number) {
                String triesStr = data.getTries().toString();

                channel.sendMessage(new MessageBuilder()
                        .append("\n`Tries: " + triesStr + "`\n\n")
                        .append("Yeah, correct guess (**`" + number + "`**), you won with `" + triesLeft + "`"
                                + (triesLeft < 2 ? " try" : " tries") + " left.\n")
                        .append("Thanks for playing!").build())
                        .queue();

                end(p, channel);
                return;
            }

            if(triesLeft > 0) {
                String triesStr = data.getTries().toString();

                Message msg_ = channel.sendMessage(new MessageBuilder()
                        .append("\n`Tries: " + triesStr + "`\n\n")
                        .append("Wrong guess! The correct number is **"
                                + (number < input ? "lower" : "higher")
                                + "** than " + input + ".\n")
                        .append("`" + triesLeft + "`" + (triesLeft < 2 ? " try" : " tries") + " left.")
                        .build())
                        .complete();

                data.setLastMsg(msg_);
            }
            else {
                String triesStr = data.getTries().toString();

                channel.sendMessage(new MessageBuilder()
                        .append("\n`Tries: " + triesStr + "`\n\n")
                        .append("Wrong guess! You lose!\n", MessageBuilder.Formatting.BOLD)
                        .append("The correct number was " + number + ".").build())
                        .queue();

                end(p, channel);
                return;
            }

            data.setTriesLeft(triesLeft);
        } catch(NumberFormatException e) {
            channel.sendMessage(new MessageBuilder()
                    .append("Sorry, this is not a number...", MessageBuilder.Formatting.ITALICS).build())
                    .queue(msg_ -> Executors.newScheduledThreadPool(1)
                            .schedule((Runnable) msg_.delete()::queue, 5, TimeUnit.SECONDS));
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
