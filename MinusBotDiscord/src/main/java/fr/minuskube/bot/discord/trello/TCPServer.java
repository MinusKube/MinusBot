package fr.minuskube.bot.discord.trello;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(TCPServer.class);
    private static final int PORT = 12915;

    private ServerSocket socket;
    private boolean running = false;

    public TCPServer() {
        try {
            socket = new ServerSocket();
            socket.bind(new InetSocketAddress("127.0.0.1", PORT));
        } catch(IOException e) {
            LOGGER.error("Couldn't create ServerSocket:", e);
        }
    }

    public void start() {
        if(socket == null || !socket.isBound())
            return;

        this.running = true;

        super.start();
    }

    @Override
    public void run() {
        while(running) {
            try {
                Socket client = socket.accept();

                LOGGER.info("Got connection from ip: " + client.getInetAddress().toString());

                try(InputStreamReader is = new InputStreamReader(client.getInputStream(), "UTF-8");
                    BufferedReader br = new BufferedReader(is)) {

                    String json = br.readLine();
                    JSONObject obj = new JSONObject(json);

                    MessageBuilder builder = new MessageBuilder();

                    JSONObject model = obj.getJSONObject("model");
                    JSONObject action = obj.getJSONObject("action");
                    JSONObject data = action.getJSONObject("data");

                    Member creator = action.has("memberCreator")
                            ? Member.from(action.getJSONObject("memberCreator")) : null;

                    Card card = data.has("card")
                            ? Card.from(data.getJSONObject("card")) : null;
                    CheckItem item = data.has("checkItem")
                            ? CheckItem.from(data.getJSONObject("checkItem")) : null;
                    Checklist list = data.has("checklist")
                            ? Checklist.from(data.getJSONObject("checklist")) : null;
                    Member member = action.has("member")
                            ? Member.from(action.getJSONObject("member")) : null;
                    JSONObject old = data.has("old")
                            ? data.getJSONObject("old") : null;


                    ActionType type = ActionType.fromName(action.getString("type"));
                    LOGGER.debug("Received Trello hook: " + type);

                    TextChannel channel = DiscordBotAPI.client().getTextChannelById("246007140253171712");

                    if(type == null)
                        builder.append("Unknown type: " + action.getString("type"));
                    else {
                        switch(type) {
                            case CARD_ADD_MEMBER: {
                                MessageEmbed embed = createEmbed(card, "Member added to card!",
                                        "** " + member.getFullName() + "** has been added to the card **"
                                                + card.getName() + "**",
                                        new Color(0, 200, 0), creator)
                                        .setThumbnail(member.getAvatarURL())
                                        .build();

                                channel.sendMessage(embed).queue();
                                break;
                            }

                            case CARD_REMOVE_MEMBER: {
                                MessageEmbed embed = createEmbed(card, "Member removed from card!",
                                        "** " + member.getFullName() + "** has been removed from the card **"
                                                + card.getName() + "**",
                                        new Color(200, 0, 0), creator)
                                        .setThumbnail(member.getAvatarURL())
                                        .build();

                                channel.sendMessage(embed).queue();
                                break;
                            }

                            case CHECKITEM_CREATE: {
                                MessageEmbed embed = createEmbed(card, "Item added to checklist!",
                                        "The item **" + item.getName()
                                                + "** has been added to the list **" + list.getName() + "**",
                                        new Color(0, 100, 200), creator)
                                        .build();

                                channel.sendMessage(embed).queue();
                                break;
                            }

                            case CHECKITEM_DELETE: {
                                MessageEmbed embed = createEmbed(card, "Item removed from checklist!",
                                        "The item **" + item.getName()
                                                + "** has been removed from the list **" + list.getName() + "**",
                                        new Color(100, 0, 200), creator)
                                        .build();

                                channel.sendMessage(embed).queue();
                                break;
                            }

                            case CHECKITEM_UPDATE: {
                                String oldName = old.getString("name");

                                MessageEmbed embed = createEmbed(card, "Checklist item updated!",
                                        "The item **" + oldName + "** in list *"
                                                + list.getName() + "* has been renamed to **"
                                                + item.getName() + "**",
                                        new Color(200, 200, 0), creator)
                                        .build();

                                channel.sendMessage(embed).queue();
                            }

                            case CHECKITEM_UPDATE_STATE: {
                                CheckItem.State state = item.getState();

                                MessageEmbed embed = createEmbed(card, "Checklist item updated!",
                                        "The item **" + item.getName() + "** in list *"
                                                + list.getName() + "* has been marked as **"
                                                + state.getName() + "**",
                                        new Color(200, 200, 0), creator)
                                        .build();

                                channel.sendMessage(embed).queue();
                                break;
                            }

                            default: {
                                String msg = obj.toString(2);

                                HttpResponse<JsonNode> resp = Unirest.post("https://hastebin.com/documents")
                                        .body(msg).asJson();

                                builder.append("https://hastebin.com/"
                                        + resp.getBody().getObject().getString("key")
                                        + "\n");
                                builder.append(type.name());
                                break;
                            }
                        }
                    }

                    if(builder.length() > 0) {
                        DiscordBotAPI.client().getTextChannelById("246007140253171712")
                                .sendMessage(builder.build()).queue();
                    }
                } catch(Exception e) {
                    LOGGER.error("Error while reading data: ", e);
                }

                client.close();
            } catch(IOException e) {
                LOGGER.error("Error while accepting socket:", e);
            }
        }
    }

    public EmbedBuilder createEmbed(Card card, String title, String description, Color color, Member creator) {
        return new EmbedBuilder()
                .setFooter(creator.getFullName() + " | on Trello",
                        creator.getAvatarURL())
                .setTitle(title)
                .setUrl("https://trello.com/c/" + card.getShortLink())
                .setDescription(description)
                .setColor(color);
    }

    public void close() {
        try {
            if(!socket.isClosed())
                socket.close();
        } catch(IOException e) {
            LOGGER.error("Couldn't close ServerSocket:", e);
        }

        running = false;
    }

    public ServerSocket getSocket() { return socket; }

}
