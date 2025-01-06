package com.pwing.guilds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;

public class GuildPerkActivateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final String perkName;
    private boolean cancelled;

    public GuildPerkActivateEvent(Guild guild, String perkName) {
        this.guild = guild;
        this.perkName = perkName;
    }

    public Guild getGuild() { return guild; }
    public String getPerkName() { return perkName; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
