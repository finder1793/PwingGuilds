package com.pwing.guilds.integrations.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

public class ExprGuildOfPlayer extends SimpleExpression<Guild> {
    private Expression<Player> player;

    @Override
    protected Guild[] get(Event event) {
        Player p = player.getSingle(event);
        if (p == null) return new Guild[0];
        return PwingGuilds.getInstance().getGuildManager().getPlayerGuild(p.getUniqueId())
                .map(guild -> new Guild[]{guild})
                .orElse(new Guild[0]);
    }

    @Override
    public boolean isSingle() {
        return true;
    }

    @Override
    public Class<? extends Guild> getReturnType() {
        return Guild.class;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    public String toString(Event e, boolean debug) {
        return "guild of player " + player.toString(e, debug);
    }
}
