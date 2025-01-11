package com.pwing.guilds.integrations.skript.expressions;

import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.skript.lang.util.SimpleExpression;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ExprGuildMembers extends SimpleExpression<Player> {
    private Expression<String> guildName;

    @Override
    protected Player[] get(Event event) {
        String name = guildName.getSingle(event);
        if (name == null) return new Player[0];
        
        return PwingGuilds.getInstance().getGuildManager().getGuild(name)
                .map(guild -> {
                    List<Player> players = new ArrayList<>();
                    for (UUID uuid : guild.getMembers()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            players.add(player);
                        }
                    }
                    return players.toArray(new Player[0]);
                })
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
        return "members of guild " + guildName.toString(e, debug);
    }
}
