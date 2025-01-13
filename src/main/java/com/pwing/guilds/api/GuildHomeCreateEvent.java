package com.pwing.guilds.api;

import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildHome;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.Location;

/**
 * Event called when a guild home is created.
 */
public class GuildHomeCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final String homeName;
    private final Location location;
    private boolean cancelled;

    /**
     * Constructs a new GuildHomeCreateEvent.
     * @param guild The guild creating the home
     * @param homeName The name of the home
     * @param location The location of the home
     */
    public GuildHomeCreateEvent(Guild guild, String homeName, Location location) {
        this.guild = guild;
        this.homeName = homeName;
        this.location = location;
    }

    /**
     * Gets the guild creating the home.
     * @return The guild
     */
    public Guild getGuild() { return guild; }

    /**
     * Gets the name of the home.
     * @return The home name
     */
    public String getHomeName() { return homeName; }

    /**
     * Gets the location of the home.
     * @return The home location
     */
    public Location getLocation() { return location; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }

    /**
     * Gets the handler list for this event.
     * @return The handler list
     */
    public static HandlerList getHandlerList() { return handlers; }
}
