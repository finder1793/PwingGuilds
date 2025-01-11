package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;

public class GuildRenameEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final String oldName;
    private String newName;
    private boolean cancelled;

    public GuildRenameEvent(Guild guild, String oldName, String newName) {
        this.guild = guild;
        this.oldName = oldName;
        this.newName = newName;
    }

    public Guild getGuild() { return guild; }
    public String getOldName() { return oldName; }
    public String getNewName() { return newName; }
    public void setNewName(String newName) { this.newName = newName; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
