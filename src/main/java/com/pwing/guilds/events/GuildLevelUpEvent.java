package com.pwing.guilds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;



public class GuildLevelUpEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final int oldLevel;
    private final int newLevel;
    private boolean cancelled;

    public GuildLevelUpEvent(Guild guild, int oldLevel, int newLevel) {
        this.guild = guild;
        this.oldLevel = oldLevel;
        this.newLevel = newLevel;
    }

    public Guild getGuild() { return guild; }
    public int getOldLevel() { return oldLevel; }
    public int getNewLevel() { return newLevel; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
