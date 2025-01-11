package com.pwing.guilds.integrations.skript.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class EffCreateGuild extends Effect {
    private Expression<String> name;
    private Expression<Player> leader;

    @Override
    protected void execute(Event event) {
        String guildName = name.getSingle(event);
        Player guildLeader = leader.getSingle(event);
        if (guildName != null && guildLeader != null) {
            PwingGuilds.getInstance().getGuildManager().createGuild(guildName, guildLeader.getUniqueId());
        }
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "create guild " + name.toString(event, debug) + " with leader " + leader.toString(event, debug);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.name = (Expression<String>) exprs[0];
        this.leader = (Expression<Player>) exprs[1];
        return true;
    }
}
