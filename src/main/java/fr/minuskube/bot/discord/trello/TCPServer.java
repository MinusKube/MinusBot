package fr.minuskube.bot.discord.trello;

import fr.minuskube.bot.discord.DiscordBotAPI;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.TextChannel;
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

    private ActionHandler handler = new ActionHandler();

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
                        builder.append("Unknown type: ").append(action.getString("type"));
                    else {
                        handler.handle(type, channel,
                                new ActionData()
                                        .withObject(obj)
                                        .withCard(card)
                                        .withCreator(creator)
                                        .withItem(item)
                                        .withList(list)
                                        .withMember(member)
                                        .withOld(old)
                        );
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
