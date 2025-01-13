package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;

/**
 * Called when a guild attempts to claim a chunk
 * Can be cancelled to prevent the claim
 */
public class GuildClaimChunkEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final ChunkLocation chunk;
    private boolean cancelled;

    /**
     * Creates a new GuildClaimChunkEvent
     * @param guild The guild attempting to claim
     * @param chunk The chunk being claimed
     */
    public GuildClaimChunkEvent(Guild guild, ChunkLocation chunk) {
        this.guild = guild;
        this.chunk = chunk;
    }

    /**
     * Gets the guild attempting to claim
     * @return The claiming guild
     */
    public Guild getGuild() { return guild; }

    /**
     * Gets the chunk being claimed
     * @return The chunk location
     */
    public ChunkLocation getChunk() { return chunk; }
    
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
