package com.pwing.guilds.integrations.skript.conditions;

import ch.njol.skript.lang.Condition;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class CondPlayerInGuild extends Condition {

    private Expression<Player> player;

    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    public boolean check(Event event) {
        Player p = player.getSingle(event);
        return p != null && PwingGuilds.getInstance().getGuildManager().getPlayerGuild(p.getUniqueId()).isPresent();
    }

    @Override 
    public String toString(Event event, boolean debug) {
        return "player " + player.toString(event, debug) + " is in guild";
    }
}
