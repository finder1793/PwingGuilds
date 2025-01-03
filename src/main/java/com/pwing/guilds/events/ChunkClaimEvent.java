package com.pwing.guilds.events;

import com.pwing.guilds.guild.Guild;
import org.bukkit.Chunk;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class ChunkClaimEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final Chunk chunk;

    public ChunkClaimEvent(Guild guild, Chunk chunk) {
        this.guild = guild;
        this.chunk = chunk;
    }

    public Guild getGuild() {
        return guild;
    }

    public Chunk getChunk() {
        return chunk;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
