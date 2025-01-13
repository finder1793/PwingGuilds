package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;

/**
 * Event called when a guild levels up.
 */
public class GuildLevelUpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final int oldLevel;
    private final int newLevel;
    private boolean cancelled;

    /**
     * Constructs a new GuildLevelUpEvent.
     * @param guild The guild leveling up
     * @param oldLevel The old level of the guild
     * @param newLevel The new level of the guild
     */
    public GuildLevelUpEvent(Guild guild, int oldLevel, int newLevel) {
        this.guild = guild;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    /**
     * Gets the guild leveling up.
     * @return The guild
     */
    public Guild getGuild() { return guild; }

    /**
     * Gets the old level of the guild.
     * @return The old level
     */
    public int getOldLevel() { return oldLevel; }

    /**
     * Gets the new level of the guild.
     * @return The new level
     */
    public int getNewLevel() { return newLevel; }
    
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
