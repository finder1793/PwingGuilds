package com.pwing.guilds.integrations.skript.conditions;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.Event;

public class CondGuildExists extends Condition {
    private Expression<String> guildName;

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.guildName = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        String name = guildName.getSingle(event);
        return name != null && PwingGuilds.getInstance().getGuildManager().getGuild(name).isPresent();
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "guild " + guildName.toString(event, debug) + " exists";
    }
}
