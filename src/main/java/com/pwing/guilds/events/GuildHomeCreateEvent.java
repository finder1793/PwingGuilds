package com.pwing.guilds.events;

import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildHome;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import org.bukkit.Location;

public class GuildHomeCreateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final String homeName;
    private final Location location;
    private boolean cancelled;

    public GuildHomeCreateEvent(Guild guild, String homeName, Location location) {
        this.guild = guild;
        this.homeName = homeName;
        this.location = location;
    }

    public Guild getGuild() { return guild; }
    public String getHomeName() { return homeName; }
    public Location getLocation() { return location; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
