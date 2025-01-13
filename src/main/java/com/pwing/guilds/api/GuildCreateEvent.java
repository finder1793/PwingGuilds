package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.pwing.guilds.guild.Guild;

/**
 * Called when a new guild is created
 * This event cannot be cancelled
 */
public class GuildCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;

    /**
     * Creates a new GuildCreateEvent
     * @param guild The newly created guild
     */
    public GuildCreateEvent(Guild guild) {
        this.guild = guild;
    }

    /**
     * Gets the newly created guild
     * @return The new guild
     */
    public Guild getGuild() {
        return guild;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the handler list for this event.
     * @return The handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
