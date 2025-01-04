package com.pwing.guilds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;
import java.util.UUID;

public class GuildMemberJoinEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final UUID player;
    private boolean cancelled;

    public GuildMemberJoinEvent(Guild guild, UUID player) {
        this.guild = guild;
        this.player = player;
    }

    public Guild getGuild() { return guild; }
    public UUID getPlayer() { return player; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
