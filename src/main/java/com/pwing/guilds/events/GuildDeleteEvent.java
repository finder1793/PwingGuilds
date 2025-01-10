package com.pwing.guilds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.pwing.guilds.guild.Guild;

/**
 * Event called when a guild is deleted.
 */
public class GuildDeleteEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;

    /**
     * Creates a new guild delete event
     * @param guild The guild being deleted
     */
    public GuildDeleteEvent(Guild guild) {
        this.guild = guild;
    }

    /**
     * Gets the guild being deleted
     * @return The guild
     */
    public Guild getGuild() {
        return guild;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the handler list for this event
     * @return The handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
