package com.pwing.guilds.alliance.events;

import com.pwing.guilds.alliance.Alliance;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Base event class for all alliance-related events
 */
public abstract class AllianceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected final Alliance alliance;

    /**
     * Creates a new AllianceEvent
     * @param alliance The alliance involved in this event
     */
    public AllianceEvent(Alliance alliance) {
        this.alliance = alliance;
    }

    /**
     * Gets the alliance involved in this event
     * @return The alliance
     */
    public Alliance getAlliance() {
        return alliance;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the handler list for this event
     * @return The handler list
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
