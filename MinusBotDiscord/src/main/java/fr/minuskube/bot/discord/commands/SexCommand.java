package fr.minuskube.bot.discord.commands;

import fr.minuskube.bot.discord.DiscordBotAPI;
import fr.minuskube.bot.discord.util.StreamUtils;
import net.dv8tion.jda.MessageBuilder;
import net.dv8tion.jda.Permission;
import net.dv8tion.jda.entities.Message;
import net.dv8tion.jda.entities.TextChannel;
import org.apache.commons.io.output.NullOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.tidy.Tidy;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Random;

public class SexCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(SexCommand.class);

    public SexCommand() {
        super("sex", "Sends a random sex image/gif.");

        this.hidden = true;
    }

    @Override
    public void execute(Message msg, String[] args) {
        String search = null;
        int amount = 1;

        if(!msg.isPrivate()) {
            TextChannel channel = ((TextChannel) msg.getChannel());
            String cName = channel.getName().toLowerCase();

            if(!cName.contains("sex") && !cName.contains("18")
                    && !cName.contains("censor") && !cName.contains("censure")) {

                msg.getChannel().sendMessage(new MessageBuilder()
                        .appendString("The pr0n is not allowed in this channel!").build());
                return;
            }

            if(!channel.checkPermission(DiscordBotAPI.self(), Permission.MESSAGE_ATTACH_FILES)) {
                channel.sendMessage(new MessageBuilder()
                        .appendString("No permission to send files!", MessageBuilder.Formatting.BOLD).build());
                return;
            }
        }

        if(msg.getAuthor().getId().equals("87279950075293696")) {
            try {
                URLConnection connection = new URL("http://humourtop.com/hommes-les-plus-moches-du-monde" +
                        "/Homme_monstrueux_humour.jpg").openConnection();
                InputStream img = connection.getInputStream();

                File tempFile = StreamUtils.tempFileFromInputStream(img, "sex-"
                        + msg.getAuthor().getUsername().toLowerCase(), ".jpg");
                msg.getChannel().sendFile(tempFile, null);
            } catch(IOException e) {
                LOGGER.error("Couldn't get image:", e);
            }

            return;
        }

        if(args.length >= 1) {
            try {
                int input = Integer.parseInt(args[0]);
                amount = Math.min(5, input);
            } catch(NumberFormatException ignored) {}

            if(args.length >= 2) {
                StringBuilder sb = new StringBuilder();

                for(int i = 1; i < args.length; i++) {
                    if(sb.length() != 0)
                        sb.append(" ");

                    sb.append(args[i]);
                }

                search = sb.toString();
            }
        }

        try {
            PrintStream oldErr = System.err;

            NullOutputStream nullOs = new NullOutputStream();
            System.setErr(new PrintStream(nullOs));

            Tidy tidy = new Tidy();

            int page = new Random().nextInt(20) + 1;

            InputStream input = new URL(search == null  ? ("http://www.sex.com/?page=" + page)
                                                        : ("http://www.sex.com/search/pictures?query=" + search))
                    .openStream();
            Document document = tidy.parseDOM(input, null);
            NodeList imgs = document.getElementsByTagName("img");

            System.setErr(oldErr);

            for(int i = 0; i < amount; i++) {
                Node item;

                int tries = 0;

                do {
                    int ri = new Random().nextInt(imgs.getLength());
                    item = imgs.item(ri);

                    tries++;
                } while((!item.hasAttributes() || item.getAttributes().getNamedItem("data-src") == null)
                        && tries < 2);

                if(!item.hasAttributes() || item.getAttributes().getNamedItem("data-src") == null) {
                    msg.getChannel().sendMessage(new MessageBuilder()
                            .appendString("No image found to satisfy your desire.").build());
                    return;
                }

                String url = (item.getAttributes().getNamedItem("data-src")
                        .getNodeValue().replace("/236/", "/620/"));
                URLConnection connection = new URL(url).openConnection();

                InputStream img = connection.getInputStream();

                File tempFile = StreamUtils.tempFileFromInputStream(img, "sex-"
                        + msg.getAuthor().getUsername().toLowerCase(), url.substring(url.length() - 4));
                msg.getChannel().sendFile(tempFile, null);
            }
        } catch(IOException e) {
            LOGGER.error("Couldn't get image:", e);
        }
    }

}
