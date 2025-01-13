package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Listener for player join and quit events.
 */
public class PlayerJoinQuitListener implements Listener {
    private final PwingGuilds plugin;

    /**
     * Creates a new PlayerJoinQuitListener.
     * @param plugin Plugin instance.
     */
    public PlayerJoinQuitListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the player join event.
     * @param event The PlayerJoinEvent.
     */
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getGuildManager().getPlayerGuild(event.getPlayer().getUniqueId())
            .ifPresent(guild -> guild.updateMemberList());
    }

    /**
     * Handles the player quit event.
     * @param event The PlayerQuitEvent.
     */
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGuildManager().getPlayerGuild(event.getPlayer().getUniqueId())
            .ifPresent(guild -> guild.updateMemberList());
    }
}
