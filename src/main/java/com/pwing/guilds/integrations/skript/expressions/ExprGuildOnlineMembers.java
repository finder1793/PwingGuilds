package com.pwing.guilds.integrations.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Expression to get the online members of a guild.
 */
public class ExprGuildOnlineMembers extends SimpleExpression<Player> {
    private Expression<String> guildName;

    /**
     * Default constructor for ExprGuildOnlineMembers.
     */
    public ExprGuildOnlineMembers() {
        // ...existing code...
    }

    @Override
    protected Player[] get(Event event) {
        String name = guildName.getSingle(event);
        if (name == null) return new Player[0];
        
        return PwingGuilds.getInstance().getGuildManager().getGuild(name)
                .map(guild -> guild.getOnlineMembers().toArray(new Player[0]))
                .orElse(new Player[0]);
    }

    @Override
    public boolean isSingle() {
        return false;
    }

    @Override
    public Class<? extends Player> getReturnType() {
        return Player.class;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.guildName = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "online members of guild " + guildName.toString(e, debug);
    }
}
