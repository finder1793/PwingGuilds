package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;
import com.pwing.guilds.guild.Guild;

/**
 * Event triggered when a guild perk is activated.
 */
public class GuildPerkActivateEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final String perkName;
    private boolean cancelled;

    /**
     * Constructs a new GuildPerkActivateEvent.
     * @param guild The guild activating the perk.
     * @param perkName The name of the perk being activated.
     */
    public GuildPerkActivateEvent(Guild guild, String perkName) {
        this.guild = guild;
        this.perkName = perkName;
    }

    /**
     * Gets the guild involved in this event.
     * @return The guild.
     */
    public Guild getGuild() { return guild; }

    /**
     * Gets the name of the perk being activated.
     * @return The perk name.
     */
    public String getPerkName() { return perkName; }
    
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
