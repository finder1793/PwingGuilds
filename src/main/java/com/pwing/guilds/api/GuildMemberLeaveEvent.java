package com.pwing.guilds.api;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import com.pwing.guilds.guild.Guild;
import java.util.UUID;

/**
 * Event fired when a member leaves a guild, either voluntarily or through
 * removal.
 */
public class GuildMemberLeaveEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private final Guild guild;
    private final UUID player;
    private final LeaveReason reason;

    /**
     * Defines the reason for the member leaving
     */
    public enum LeaveReason {
        /** Member left voluntarily */
        QUIT,
        /** Member was kicked by leadership */
        KICKED,
        /** Member was banned from the guild */
        BANNED,
        /** Guild was disbanded */
        DISBANDED,
        /** Member has Left the Guild */
        LEFT,

    }

    /**
     * Creates a new member leave event
     * 
     * @param guild  The guild the player is leaving
     * @param player The UUID of the leaving player
     * @param reason The reason for leaving
     */
    public GuildMemberLeaveEvent(Guild guild, UUID player, LeaveReason reason) {
        this.guild = guild;
        this.player = player;
        this.reason = reason;
    }

    /**
     * Gets the guild the player is leaving
     * 
     * @return The guild instance
     */
    public Guild getGuild() {
        return guild;
    }

    /**
     * Gets the UUID of the leaving player
     * 
     * @return The player's UUID
     */
    public UUID getPlayer() {
        return player;
    }

    /**
     * Gets the reason for the member leaving
     * 
     * @return The leave reason enum value
     */
    public LeaveReason getReason() {
        return reason;
    }

    /**
     * Gets the handler list for this event
     * 
     * @return The static handler list
     */
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * Gets the handler list for this event.
     * @return The handler list.
     */
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
