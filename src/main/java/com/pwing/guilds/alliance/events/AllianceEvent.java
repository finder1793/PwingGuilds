package com.pwing.guilds.alliance.events;

import com.pwing.guilds.alliance.Alliance;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public abstract class AllianceEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    protected final Alliance alliance;

    public AllianceEvent(Alliance alliance) {
        this.alliance = alliance;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
