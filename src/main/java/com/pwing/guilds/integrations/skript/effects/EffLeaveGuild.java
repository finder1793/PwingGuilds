package com.pwing.guilds.integrations.skript.effects;

import ch.njol.skript.lang.Effect;
import ch.njol.skript.lang.Expression;
import ch.njol.skript.lang.SkriptParser.ParseResult;
import ch.njol.util.Kleenean;
import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.api.GuildMemberLeaveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;

/**
 * Effect to leave a guild.
 */
public class EffLeaveGuild extends Effect {
    private Expression<Player> player;

    /**
     * Default constructor for EffLeaveGuild.
     */
    public EffLeaveGuild() {
        // ...existing code...
    }

    @Override
    protected void execute(Event event) {
        Player p = player.getSingle(event);
        if (p != null) {
            PwingGuilds.getInstance().getGuildManager().getPlayerGuild(p.getUniqueId())
                    .ifPresent(guild -> guild.removeMember(p.getUniqueId(), GuildMemberLeaveEvent.LeaveReason.LEFT));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean init(Expression<?>[] exprs, int matchedPattern, Kleenean isDelayed, ParseResult parseResult) {
        this.player = (Expression<Player>) exprs[0];
        return true;
    }

    @Override
    public String toString(Event event, boolean debug) {
        return "make " + player.toString(event, debug) + " leave their guild";
    }
}
