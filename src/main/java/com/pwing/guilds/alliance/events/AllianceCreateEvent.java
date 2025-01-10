package com.pwing.guilds.alliance.events;

import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.guild.Guild;
import org.bukkit.event.Cancellable;

/**
 * Event called when a new alliance is created
 */
public class AllianceCreateEvent extends AllianceEvent implements Cancellable {
    private boolean cancelled;
    private final Guild founder;

    /**
     * Creates a new AllianceCreateEvent
     * @param alliance The alliance being created
     * @param founder The guild founding the alliance
     */
    public AllianceCreateEvent(Alliance alliance, Guild founder) {
        super(alliance);
        this.founder = founder;
    }

    /**
     * Gets the guild that is founding this alliance
     * @return The founding guild
     */
    public Guild getFounder() {
        return founder;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
