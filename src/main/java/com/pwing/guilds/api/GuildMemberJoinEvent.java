package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;
import java.util.UUID;

/**
 * Event triggered when a player joins a guild.
 */
public class GuildMemberJoinEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final UUID player;
    private boolean cancelled;

    /**
     * Constructs a new GuildMemberJoinEvent.
     * @param guild The guild the player is joining.
     * @param player The UUID of the player joining the guild.
     */
    public GuildMemberJoinEvent(Guild guild, UUID player) {
        this.guild = guild;
        this.player = player;
    }

    /**
     * Gets the guild involved in this event.
     * @return The guild.
     */
    public Guild getGuild() { return guild; }

    /**
     * Gets the player involved in this event.
     * @return The player's UUID.
     */
    public UUID getPlayer() { return player; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }

    /**
     * Gets the handler list for this event.
     * @return The handler list.
     */
    public static HandlerList getHandlerList() { return handlers; }
}
