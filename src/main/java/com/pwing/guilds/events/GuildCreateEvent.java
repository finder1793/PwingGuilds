package com.pwing.guilds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.pwing.guilds.guild.Guild;

public class GuildCreateEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;

    public GuildCreateEvent(Guild guild) {
        this.guild = guild;
    }

    public Guild getGuild() {
        return guild;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
