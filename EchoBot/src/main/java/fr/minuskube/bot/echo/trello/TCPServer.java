package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.core.MessageBuilder;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

                    ActionType type = ActionType.fromName(action.getString("type"));

                    if(type == null)
                        builder.appendString("Unknown type: " + action.getString("type"));
                    else {
                        switch(type) {
                            case CARD_ADD_MEMBER: {
                                Card card = Card.from(data.getJSONObject("card"));
                                Member creator = Member.from(action.getJSONObject("memberCreator"));
                                Member member = Member.from(action.getJSONObject("member"));

                                new TestImage(DiscordBotAPI.client().getTextChannelById("246007140253171712"),
                                        card, member, true).sendImage();

                                break;
                            }

                            case CARD_REMOVE_MEMBER: {
                                Card card = Card.from(data.getJSONObject("card"));
                                Member creator = Member.from(action.getJSONObject("memberCreator"));
                                Member member = Member.from(action.getJSONObject("member"));

                                new TestImage(DiscordBotAPI.client().getTextChannelById("246007140253171712"),
                                        card, member, false).sendImage();

                                break;
                            }

                            default: {
                                builder.appendCodeBlock(obj.toString(2), "json");
                                builder.appendString(type.name());
                                break;
                            }
                        }
                    }

                    if(builder.getLength() > 0)
                        DiscordBotAPI.client().getTextChannelById("246007140253171712")
                                .sendMessage(builder.build()).queue();
                } catch(Exception e) {
                    LOGGER.error("Error while reading data: ", e);
                }

                client.close();
            } catch(IOException e) {
                LOGGER.error("Error while accepting socket:", e);
            }
        }
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
