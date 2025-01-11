package com.pwing.guilds.api;

import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.guild.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

/**
 * Event fired when a guild leaves an alliance.
 * Can be cancelled to prevent the guild from leaving.
 */
public class GuildLeaveAllianceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final Alliance alliance;
    private boolean cancelled;

    /**
     * Creates a new guild leave alliance event
     * @param guild The guild leaving the alliance
     * @param alliance The alliance being left
     */
    public GuildLeaveAllianceEvent(Guild guild, Alliance alliance) {
        this.guild = guild;
        this.alliance = alliance;
    }

    /**
     * Gets the guild leaving the alliance
     * @return The departing guild
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Gets the alliance being left
     * @return The alliance the guild is leaving
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
     * Gets the HandlerList for this event
     * @return The static event handlers
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
