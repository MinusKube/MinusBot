package fr.minuskube.bot.echo.util;

import fr.minuskube.bot.echo.EchoBotAPI;
import net.dv8tion.jda.core.entities.EntityBuilder;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.requests.Request;
import net.dv8tion.jda.core.requests.Response;
import net.dv8tion.jda.core.requests.RestAction;
import net.dv8tion.jda.core.requests.Route;
import org.json.JSONObject;

public class EmbedMessage {

    public static RestAction<Message> send(MessageChannel channel, Message msg, JSONObject embed) {
        final Route.CompiledRoute route = Route.Messages.SEND_MESSAGE.compile(channel.getId());
        final JSONObject json = new JSONObject()
                .put("content", msg != null ? msg.getRawContent() : null)
                .put("tts", msg != null ? msg.isTTS() : null)
                .put("embed", embed);

        return new RestAction<Message>(EchoBotAPI.client(), route, json) {
            @SuppressWarnings("unchecked")
            protected void handleResponse(Response response, Request request) {
                if(response.isOk()) {
                    Message m = EntityBuilder.get(EchoBotAPI.client()).createMessage(response.getObject());
                    request.onSuccess(m);
                } else {
                    request.onFailure(response);
                }
            }
        };
    }

}
