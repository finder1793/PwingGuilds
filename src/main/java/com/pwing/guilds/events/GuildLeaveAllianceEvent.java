package com.pwing.guilds.events;

import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.guild.Guild;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Cancellable;

public class GuildLeaveAllianceEvent extends Event implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final Alliance alliance;
    private boolean cancelled;

    public GuildLeaveAllianceEvent(Guild guild, Alliance alliance) {
        this.guild = guild;
        this.alliance = alliance;
    }

    public Guild getGuild() {
        return guild;
    }

    public Alliance getAlliance() {
        return alliance;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
