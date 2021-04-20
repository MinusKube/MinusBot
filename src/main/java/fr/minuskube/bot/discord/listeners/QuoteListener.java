package fr.minuskube.bot.discord.listeners;

import fr.minuskube.bot.discord.DiscordBot;
import fr.minuskube.bot.discord.util.Quote;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QuoteListener extends Listener {

    private static final Logger LOGGER = LoggerFactory.getLogger(QuoteListener.class);

    public QuoteListener(DiscordBot bot) {
        super(bot);
    }

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent e) {
        MessageReaction.ReactionEmote emote = e.getReaction().getReactionEmote();

        if(e.getChannel().getType() != ChannelType.TEXT)
            return;

        TextChannel channel = (TextChannel) e.getChannel();
        Guild guild = channel.getGuild();
        Member member = guild.retrieveMember(e.getUser()).complete();

        Quote quote = Quote.fromMessageId(e.getMessageId());

        if(quote == null)
            return;
        if(quote.getAsker() != member)
            return;

        if(emote.getName().equals(Quote.EMOTE_NEXT) && quote.hasNext()) {
            quote.next();
            e.getReaction().removeReaction(e.getUser()).queue();
        }
        else if(emote.getName().equals(Quote.EMOTE_REMOVE))
            quote.delete();
    }

}
