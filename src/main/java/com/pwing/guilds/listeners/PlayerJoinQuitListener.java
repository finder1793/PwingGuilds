package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerJoinQuitListener implements Listener {
    private final PwingGuilds plugin;

    public PlayerJoinQuitListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        plugin.getGuildManager().getPlayerGuild(event.getPlayer().getUniqueId())
            .ifPresent(guild -> guild.updateMemberList());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        plugin.getGuildManager().getPlayerGuild(event.getPlayer().getUniqueId())
            .ifPresent(guild -> guild.updateMemberList());
    }
}
