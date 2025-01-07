package com.pwing.guilds.alliance.events;

import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.guild.Guild;
import org.bukkit.event.Cancellable;

public class AllianceCreateEvent extends AllianceEvent implements Cancellable {
    private boolean cancelled;
    private final Guild founder;

    public AllianceCreateEvent(Alliance alliance, Guild founder) {
        super(alliance);
        this.founder = founder;
    }

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
