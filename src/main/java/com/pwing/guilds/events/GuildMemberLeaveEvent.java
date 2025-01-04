package com.pwing.guilds.events;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.pwing.guilds.guild.Guild;
import java.util.UUID;

public class GuildMemberLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final UUID player;
    private final LeaveReason reason;

    public enum LeaveReason {
        QUIT, KICKED, BANNED
    }

    public GuildMemberLeaveEvent(Guild guild, UUID player, LeaveReason reason) {
        this.guild = guild;
        this.player = player;
        this.reason = reason;
    }

    public Guild getGuild() { return guild; }
    public UUID getPlayer() { return player; }
    public LeaveReason getReason() { return reason; }
    
    @Override
    public HandlerList getHandlers() { return handlers; }
    public static HandlerList getHandlerList() { return handlers; }
}
