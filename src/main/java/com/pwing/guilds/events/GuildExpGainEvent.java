package com.pwing.guilds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;

public class GuildExpGainEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private long amount;
    private boolean cancelled;

    public GuildExpGainEvent(Guild guild, long amount) {
        this.guild = guild;
        this.amount = amount;
    }

    public Guild getGuild() { return guild; }
    public long getAmount() { return amount; }
    public void setAmount(long amount) { this.amount = amount; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
