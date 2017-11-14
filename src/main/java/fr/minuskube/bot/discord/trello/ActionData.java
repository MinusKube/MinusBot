package fr.minuskube.bot.discord.trello;

import org.json.JSONObject;

public class ActionData {

    private JSONObject object;
    private Member creator;
    private Card card;
    private CheckItem item;
    private Checklist list;
    private Member member;
    private JSONObject old;

    public ActionData withObject(JSONObject object) {
        this.object = object;
        return this;
    }

    public ActionData withCreator(Member creator) {
        this.creator = creator;
        return this;
    }

    public ActionData withCard(Card card) {
        this.card = card;
        return this;
    }

    public ActionData withItem(CheckItem item) {
        this.item = item;
        return this;
    }

    public ActionData withList(Checklist list) {
        this.list = list;
        return this;
    }

    public ActionData withMember(Member member) {
        this.member = member;
        return this;
    }

    public ActionData withOld(JSONObject old) {
        this.old = old;
        return this;
    }

    public JSONObject getObject() { return object; }
    public Member getCreator() { return creator; }
    public Card getCard() { return card; }
    public CheckItem getItem() { return item; }
    public Checklist getList() { return list; }
    public Member getMember() { return member; }
    public JSONObject getOld() { return old; }

}
