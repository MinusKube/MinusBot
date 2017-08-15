package fr.minuskube.bot.discord.trello;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.MessageBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;
import net.dv8tion.jda.core.entities.TextChannel;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

public class ActionHandler {

    private List<String> cancelItemUpdateState = new ArrayList<>();

    public void handle(ActionType type, TextChannel channel, ActionData data) throws UnirestException {
        switch(type) {
            case CARD_ADD_MEMBER: { handleCardAddMember(channel, data); break; }
            case CARD_REMOVE_MEMBER:  { handleCardRemoveMember(channel, data); break; }
            case CHECKITEM_CREATE:  { handleCheckItemCreate(channel, data); break; }
            case CHECKITEM_DELETE:  { handleCheckItemDelete(channel, data); break; }
            case CHECKITEM_UPDATE:  { handleCheckItemUpdate(channel, data); break; }
            case CHECKITEM_UPDATE_STATE:  { handleCheckItemUpdateState(channel, data); break; }

            default: {
                String msg = data.getObject().toString(2);

                HttpResponse<JsonNode> resp = Unirest.post("https://hastebin.com/documents")
                        .body(msg).asJson();

                channel.sendMessage(new MessageBuilder().append("https://hastebin.com/")
                        .append(resp.getBody().getObject().getString("key")).append("\n")
                        .append(type.name())
                        .build()).queue();
                break;
            }
        }
    }

    private void handleAttachmentAdd(TextChannel channel, ActionData data) {}
    private void handleAttachmentDelete(TextChannel channel, ActionData data) {}
    private void handleBoardAddMember(TextChannel channel, ActionData data) {}
    private void handleBoardRemoveMember(TextChannel channel, ActionData data) {}
    private void handleBoardUpdate(TextChannel channel, ActionData data) {}

    private void handleCardAddMember(TextChannel channel, ActionData data) {
        Card card = data.getCard();

        MessageEmbed embed = createEmbed(card, "Member added to card!",

                "**[" + card.getName() + "]**\n\n"
                        + "**" + data.getMember().getFullName() + " (" + data.getMember().getUsername() + ")**",

                new Color(0, 200, 0),
                data.getCreator())

                .setImage(data.getMember().getAvatarURL())

                .setThumbnail("http://minuskube.fr/images/bot/icons/user-plus-green.png")
                .build();

        channel.sendMessage(embed).queue();
    }

    private void handleCardRemoveMember(TextChannel channel, ActionData data) {
        Card card = data.getCard();

        MessageEmbed embed = createEmbed(card, "Member removed from card",

                "**[" + card.getName() + "]**\n\n"
                        + "**" + data.getMember().getFullName() + " (" + data.getMember().getUsername() + ")**",

                new Color(200, 0, 0),
                data.getCreator())

                .setImage(data.getMember().getAvatarURL())

                .setThumbnail("http://minuskube.fr/images/bot/icons/user-times-red.png")
                .build();

        channel.sendMessage(embed).queue();
    }

    private void handleCardUpdate(TextChannel channel, ActionData data) {}
    private void handleCardCopy(TextChannel channel, ActionData data) {}
    private void handleCardCreate(TextChannel channel, ActionData data) {}
    private void handleCardCreateFromCheckItem(TextChannel channel, ActionData data) {}
    private void handleCardDelete(TextChannel channel, ActionData data) {}
    private void handleCardEmail(TextChannel channel, ActionData data) {}
    private void handleCardMoveFromBoard(TextChannel channel, ActionData data) {}
    private void handleCardMoveToBoard(TextChannel channel, ActionData data) {}

    private void handleCheckItemCreate(TextChannel channel, ActionData data) {
        Card card = data.getCard();

        MessageEmbed embed = createEmbed(card, "Checklist item added",

                "**[" + card.getName() + "]** - " + data.getList().getName() + "\n\n"
                        + "**" + data.getItem().getName() + "**",

                new Color(0, 200, 0),
                data.getCreator())

                .setThumbnail("http://minuskube.fr/images/bot/icons/square-plus-green.png")
                .build();

        channel.sendMessage(embed).queue();
    }

    private void handleCheckItemDelete(TextChannel channel, ActionData data) {
        Card card = data.getCard();

        MessageEmbed embed = createEmbed(card, "Checklist item removed",

                "**[" + card.getName() + "]** - " + data.getList().getName() + "\n\n"
                        + "**" + data.getItem().getName() + "**",

                new Color(200, 0, 0),
                data.getCreator())

                .setThumbnail("http://minuskube.fr/images/bot/icons/square-times-red.png")
                .build();

        channel.sendMessage(embed).queue();
    }

    private void handleCheckItemUpdate(TextChannel channel, ActionData data) {
        Card card = data.getCard();
        String oldName = data.getOld().getString("name");

        MessageEmbed embed = createEmbed(card, "Checklist item renamed",

                "**[" + card.getName() + "]** - " + data.getList().getName() + "\n\n"
                        + "Before: **" + oldName + "**\n"
                        + "After: **" + data.getItem().getName() + "**",

                new Color(80, 150, 200),
                data.getCreator())

                .setThumbnail("http://minuskube.fr/images/bot/icons/square-wrench-blue.png")
                .build();

        channel.sendMessage(embed).queue();

        cancelItemUpdateState.add(data.getItem().getId());
    }

    private void handleCheckItemUpdateState(TextChannel channel, ActionData data) {
        String itemId = data.getItem().getId();

        if(cancelItemUpdateState.contains(itemId)) {
            cancelItemUpdateState.remove(itemId);
            return;
        }

        Card card = data.getCard();
        CheckItem.State state = data.getItem().getState();

        MessageEmbed embed = null;

        switch(state) {
            case COMPLETE: {
                embed = createEmbed(card, "Checklist item completed",

                        "**[" + card.getName() + "]** - " + data.getList().getName() + "\n\n"
                                + "**" + data.getItem().getName() + "**",

                        new Color(0, 200, 0),
                        data.getCreator())

                        .setThumbnail("http://minuskube.fr/images/bot/icons/square-check-green.png")
                        .build();

                break;
            }

            case INCOMPLETE: {
                embed = createEmbed(card, "Checklist item uncompleted",

                        "**[" + card.getName() + "]** - " + data.getList().getName() + "\n\n"
                                + "**" + data.getItem().getName() + "**",

                        new Color(200, 0, 0),
                        data.getCreator())

                        .setThumbnail("http://minuskube.fr/images/bot/icons/square-gray.png?v=2")
                        .build();

                break;
            }
        }

        channel.sendMessage(embed).queue();
    }

    private void handleChecklistAdd(TextChannel channel, ActionData data) {}
    private void handleChecklistDelete(TextChannel channel, ActionData data) {}
    private void handleChecklistUpdate(TextChannel channel, ActionData data) {}
    private void handleCommentAdd(TextChannel channel, ActionData data) {}
    private void handleCommentDelete(TextChannel channel, ActionData data) {}
    private void handleCommentUpdate(TextChannel channel, ActionData data) {}
    private void handleLabelCreate(TextChannel channel, ActionData data) {}
    private void handleLabelDelete(TextChannel channel, ActionData data) {}
    private void handleLabelAdd(TextChannel channel, ActionData data) {}
    private void handleLabelRemove(TextChannel channel, ActionData data) {}
    private void handleLabelUpdate(TextChannel channel, ActionData data) {}
    private void handleListCreate(TextChannel channel, ActionData data) {}
    private void handleListMoveFromBoard(TextChannel channel, ActionData data) {}
    private void handleListMoveToBoard(TextChannel channel, ActionData data) {}
    private void handleListUpdate(TextChannel channel, ActionData data) {}

    public EmbedBuilder createEmbed(Card card, String title, String description, Color color, Member creator) {
        return new EmbedBuilder()
                .setFooter(creator.getFullName() + " | on Trello",
                        creator.getAvatarURL())
                .setTitle(title, "https://trello.com/c/" + card.getShortLink())
                .setDescription(description)
                .setColor(color);
    }

}
