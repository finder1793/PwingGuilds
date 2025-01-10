package com.pwing.guilds.events;

import com.pwing.guilds.guild.Guild;
import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event that is called when a guild claims a chunk of land
 */
public class ChunkClaimEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final Chunk chunk;

    /**
     * Constructs a new ChunkClaimEvent
     * @param guild The guild claiming the chunk
     * @param chunk The chunk being claimed
     */
    public ChunkClaimEvent(Guild guild, Chunk chunk) {
        this.guild = guild;
        this.chunk = chunk;
    }

    /**
     * Gets the guild that is claiming the chunk
     * @return The claiming guild
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Gets the chunk being claimed
     * @return The claimed chunk
     */
    public Chunk getChunk() {
        return chunk;
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
