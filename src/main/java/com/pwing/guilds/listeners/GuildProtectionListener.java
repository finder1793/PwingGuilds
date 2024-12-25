package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class GuildProtectionListener implements Listener {
    private final PwingGuilds plugin;

    public GuildProtectionListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        plugin.getGuildManager().getGuildByChunk(event.getBlock().getChunk())
                .ifPresent(guild -> {
                    if (!guild.getMembers().contains(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§cThis area is protected by " + guild.getName());
                    }
                });
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        plugin.getGuildManager().getGuildByChunk(event.getBlock().getChunk())
                .ifPresent(guild -> {
                    if (!guild.getMembers().contains(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§cThis area is protected by " + guild.getName());
                    }
                });
    }
}