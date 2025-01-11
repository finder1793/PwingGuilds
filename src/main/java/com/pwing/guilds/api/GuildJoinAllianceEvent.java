package com.pwing.guilds.api;

import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.guild.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

/**
 * Called when a guild joins an alliance
 * Can be cancelled to prevent the alliance join
 */
public class GuildJoinAllianceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled;
    private final Guild guild;
    private final Alliance alliance;

    /**
     * Creates a new guild join alliance event
     * @param guild The guild joining
     * @param alliance The alliance being joined
     */
    public GuildJoinAllianceEvent(Guild guild, Alliance alliance) {
        this.guild = guild;
        this.alliance = alliance;
    }

    /**
     * Gets the guild joining the alliance
     * @return The joining guild
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Gets the alliance being joined
     * @return The alliance
     */
    public Alliance getAlliance() {
        return alliance;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the handler list for this event
     * @return Event handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
