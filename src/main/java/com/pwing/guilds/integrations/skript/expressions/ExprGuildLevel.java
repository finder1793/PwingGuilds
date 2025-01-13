package com.pwing.guilds.integrations.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.Event;

/**
 * Expression to get the level of a guild.
 */
public class ExprGuildLevel extends SimpleExpression<Number> {
    private Expression<String> guildName;

    /**
     * Default constructor for ExprGuildLevel.
     */
    public ExprGuildLevel() {
        // ...existing code...
    }

    @Override
    protected Number[] get(Event event) {
        String name = guildName.getSingle(event);
        if (name == null) return new Number[0];
        return PwingGuilds.getInstance().getGuildManager().getGuild(name)
                .map(guild -> new Number[]{guild.getLevel()})
                .orElse(new Number[0]);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Number> getReturnType() {
        return Number.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.guildName = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "level of guild " + guildName.toString(e, debug);
    }
}
