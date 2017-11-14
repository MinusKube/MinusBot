package fr.minuskube.bot.discord.trello;

public enum ActionType {

    ATTACHMENT_ADD("addAttachmentToCard"),
    ATTACHMENT_DELETE("deleteAttachmentFromCard"),
    BOARD_ADD_MEMBER("addMemberToBoard"),
    BOARD_REMOVE_MEMBER("removeMemberFromBoard"),
    BOARD_UPDATE("updateBoard"),
    CARD_ADD_MEMBER("addMemberToCard"),
    CARD_REMOVE_MEMBER("removeMemberFromCard"),
    CARD_UPDATE("updateCard"),
    CARD_COPY("copyCard"),
    CARD_CREATE("createCard"),
    CARD_CREATE_FROM_CHECKITEM("convertToCardFromCheckItem"),
    CARD_DELETE("deleteCard"),
    CARD_EMAIL("emailCard"),
    CARD_MOVE_FROM_BOARD("moveCardFromBoard"),
    CARD_MOVE_TO_BOARD("moveCardToBoard"),
    CHECKITEM_CREATE("createCheckItem"),
    CHECKITEM_DELETE("deleteCheckItem"),
    CHECKITEM_UPDATE("updateCheckItem"),
    CHECKITEM_UPDATE_STATE("updateCheckItemStateOnCard"),
    CHECKLIST_ADD("addChecklistToCard"),
    CHECKLIST_DELETE("removeChecklistFromCard"),
    CHECKLIST_UPDATE("updateChecklist"),
    COMMENT_ADD("commentCard"),
    COMMENT_DELETE("deleteComment"),
    COMMENT_UPDATE("updateComment"),
    LABEL_CREATE("createLabel"),
    LABEL_DELETE("deleteLabel"),
    LABEL_ADD("addLabelToCard"),
    LABEL_REMOVE("removeLabelFromCard"),
    LABEL_UPDATE("updateLabel"),
    LIST_CREATE("createList"),
    LIST_MOVE_FROM_BOARD("moveListFromBoard"),
    LIST_MOVE_TO_BOARD("moveListToBoard"),
    LIST_UPDATE("updateList");

    private String name;

    ActionType(String name) {
        this.name = name;
    }

    public String getName() { return name; }

    public static ActionType fromName(String name) {
        for(ActionType type : values())
            if(type.getName().equalsIgnoreCase(name))
                return type;

        return null;
    }

}
