package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;

/**
 * Event triggered when a guild is renamed.
 */
public class GuildRenameEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final String oldName;
    private String newName;
    private boolean cancelled;

    /**
     * Constructs a new GuildRenameEvent.
     * @param guild The guild being renamed.
     * @param oldName The old name of the guild.
     * @param newName The new name of the guild.
     */
    public GuildRenameEvent(Guild guild, String oldName, String newName) {
        this.guild = guild;
        this.oldName = oldName;
        this.newName = newName;
    }

    /**
     * Gets the guild being renamed.
     * @return The guild.
     */
    public Guild getGuild() { return guild; }

    /**
     * Gets the old name of the guild.
     * @return The old name.
     */
    public String getOldName() { return oldName; }

    /**
     * Gets the new name of the guild.
     * @return The new name.
     */
    public String getNewName() { return newName; }

    /**
     * Sets the new name of the guild.
     * @param newName The new name.
     */
    public void setNewName(String newName) { this.newName = newName; }
    
    @Override
    public boolean isCancelled() { return cancelled; }
    @Override
    public void setCancelled(boolean cancel) { this.cancelled = cancel; }
    @Override
    public HandlerList getHandlers() { return handlers; }

    /**
     * Gets the handler list for this event.
     * @return The handler list.
     */
    public static HandlerList getHandlerList() { return handlers; }
}
