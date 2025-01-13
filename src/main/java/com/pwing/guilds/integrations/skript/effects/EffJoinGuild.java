package com.pwing.guilds.integrations.skript.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Effect to join a guild.
 */
public class EffJoinGuild extends Effect {
    /**
     * Default constructor for EffJoinGuild.
     */
    public EffJoinGuild() {
        // ...existing code...
    }

    private Expression<Player> player;
    private Expression<String> guildName;

    @Override
    protected void execute(Event event) {
        Player p = player.getSingle(event);
        String name = guildName.getSingle(event);
        if (p != null && name != null) {
            PwingGuilds.getInstance().getGuildManager().getGuild(name)
                    .ifPresent(guild -> guild.addMember(p.getUniqueId()));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.player = (Expression<Player>) exprs[0];
        this.guildName = (Expression<String>) exprs[1];
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "make " + player.toString(event, debug) + " join guild " + guildName.toString(event, debug);
    }
}
