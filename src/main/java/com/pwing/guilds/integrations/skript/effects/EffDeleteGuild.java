package com.pwing.guilds.integrations.skript.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.Event;

/**
 * Effect to delete a guild.
 */
public class EffDeleteGuild extends Effect {
    private Expression<String> guildName;

    /**
     * Default constructor for EffDeleteGuild.
     */
    public EffDeleteGuild() {
        // ...existing code...
    }

    @Override
    protected void execute(Event event) {
        String name = guildName.getSingle(event);
        if (name != null) {
            PwingGuilds.getInstance().getGuildManager().deleteGuild(name);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.guildName = (Expression<String>) exprs[0];
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "delete guild " + guildName.toString(event, debug);
    }
}
