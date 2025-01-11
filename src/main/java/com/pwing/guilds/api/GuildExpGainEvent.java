package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;

/**
 * Called when a guild gains experience points
 * Can be cancelled to prevent the exp gain
 */
public class GuildExpGainEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private long amount;
    private boolean cancelled;

    /**
     * Creates a new guild exp gain event
     * @param guild Guild receiving exp
     * @param amount Amount of exp to add
     */
    public GuildExpGainEvent(Guild guild, long amount) {
        this.guild = guild;
        this.amount = amount;
    }

    /**
     * Gets the guild receiving exp
     * @return The guild
     */
    public Guild getGuild() { return guild; }

    /**
     * Gets the amount of exp being added
     * @return Experience amount
     */
    public long getAmount() { return amount; }

    /**
     * Sets the amount of exp to add
     * @param amount New exp amount
     */
    public void setAmount(long amount) { this.amount = amount; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }

    /**
     * Gets the handler list for this event
     * @return Event handler list
     */
    public static HandlerList getHandlerList() { return handlers; }
}
