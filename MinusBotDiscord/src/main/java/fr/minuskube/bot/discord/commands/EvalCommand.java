package fr.minuskube.bot.discord.commands;

import jdk.nashorn.api.scripting.NashornScriptEngineFactory;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.util.Arrays;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class EvalCommand extends Command {

    private static final Logger LOGGER = LoggerFactory.getLogger(EvalCommand.class);
    private static final long TIMEOUT = 10 * 1000;

    public EvalCommand() {
        super("eval", "Runs some code.", "<language> <code>");
    }

    @Override
    public void execute(Message msg, String[] args) {
        MessageChannel channel = msg.getChannel();

        if(msg.getChannelType() == ChannelType.TEXT) {
            TextChannel tc = (TextChannel) channel;

            if(tc.getGuild().getSelfMember().hasPermission(tc, Permission.MESSAGE_MANAGE))
                msg.deleteMessage().queue();
        }

        String language = args[0];
        String code = String.join(" ", (CharSequence[]) Arrays.copyOfRange(args, 1, args.length));

        switch(language.toLowerCase()) {
            case "php": {
                channel.sendMessage("The PHP eval has temporary been disabled.").queue();
                /*String input = "Input (PHP): ```php\n" + code + "```\n";

                channel.sendMessage(input + "Result: `Loading...`")
                        .queue(message -> {
                            try {
                                Unirest.post("http://phptester.net/")
                                        .field("phpcode", " " + code).asStringAsync()
                                        .get(TIMEOUT, TimeUnit.MILLISECONDS);

                                HttpResponse<String> response = Unirest.get("http://phptester.net/code.php70")
                                        .asStringAsync().get(TIMEOUT, TimeUnit.MILLISECONDS);

                                String resp = response.getBody();

                                if(resp.length() > 1500)
                                    resp = resp.substring(0, 1500);

                                message.editMessage(input + "Result: ```\n" + resp + "```").queue();
                            } catch(InterruptedException | ExecutionException e) {
                                message.editMessage(input + "Result: ```Error: " + e.getMessage() + "```").queue();
                            } catch(TimeoutException e) {
                                message.editMessage(input + "Result: `Timed out!`").queue();
                            }
                        });*/

                break;
            }

            case "js": {
                String input = "Input (JavaScript): ```js\n" + code + "```\n";

                channel.sendMessage(input + "Result: `Loading...`")
                        .queue(message -> {
                            ScriptEngine engine = new NashornScriptEngineFactory().getScriptEngine("--no-java");

                            Callable<Object> call = () -> engine.eval(code);
                            Future<Object> future = Executors.newCachedThreadPool().submit(call);

                            try {
                                Object result = future.get(TIMEOUT, TimeUnit.MILLISECONDS);
                                message.editMessage(input + "Result: ```\n" + result + "```").queue();
                            } catch(InterruptedException | ExecutionException e) {
                                message.editMessage(input + "Result: ```Error: " + e.getMessage() + "```").queue();
                            } catch(TimeoutException e) {
                                message.editMessage(input + "Result: `Timed out!`").queue();
                            }
                        });

                break;
            }

            case "debug": {
                ScriptEngineManager manager = new ScriptEngineManager();
                channel.sendMessage(manager.getEngineFactories().toString()).queue();

                break;
            }

            default: {
                channel.sendMessage("This language is not supported!\n**Supported languages:** " +
                        "PHP, JS").queue();
                break;
            }
        }
    }

    @Override
    public boolean checkSyntax(Message msg, String[] args) {
        return args.length >= 2;
    }

}
