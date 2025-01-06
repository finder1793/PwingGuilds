package com.pwing.guilds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;

public class GuildClaimChunkEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final ChunkLocation chunk;
    private boolean cancelled;

    public GuildClaimChunkEvent(Guild guild, ChunkLocation chunk) {
        this.guild = guild;
        this.chunk = chunk;
    }

    public Guild getGuild() { return guild; }
    public ChunkLocation getChunk() { return chunk; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
