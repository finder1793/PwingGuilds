package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.hanging.HangingBreakByEntityEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerBucketFillEvent;

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

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getClickedBlock() == null) return;

        plugin.getGuildManager().getGuildByChunk(event.getClickedBlock().getChunk())
                .ifPresent(guild -> {
                    if (!guild.getMembers().contains(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§cThis area is protected by " + guild.getName());
                    }
                });
    }

    @EventHandler
    public void onBucketEmpty(PlayerBucketEmptyEvent event) {
        plugin.getGuildManager().getGuildByChunk(event.getBlock().getChunk())
                .ifPresent(guild -> {
                    if (!guild.getMembers().contains(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§cThis area is protected by " + guild.getName());
                    }
                });
    }

    @EventHandler
    public void onBucketFill(PlayerBucketFillEvent event) {
        plugin.getGuildManager().getGuildByChunk(event.getBlock().getChunk())
                .ifPresent(guild -> {
                    if (!guild.getMembers().contains(event.getPlayer().getUniqueId())) {
                        event.setCancelled(true);
                        event.getPlayer().sendMessage("§cThis area is protected by " + guild.getName());
                    }
                });
    }

    @EventHandler
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;

        plugin.getGuildManager().getGuildByChunk(event.getEntity().getLocation().getChunk())
                .ifPresent(guild -> {
                    if (!guild.getMembers().contains(player.getUniqueId())) {
                        event.setCancelled(true);
                        player.sendMessage("§cThis area is protected by " + guild.getName());
                    }
                });
    }

    @EventHandler
    public void onHangingBreak(HangingBreakByEntityEvent event) {
        if (!(event.getRemover() instanceof Player player)) return;

        plugin.getGuildManager().getGuildByChunk(event.getEntity().getLocation().getChunk())
                .ifPresent(guild -> {
                    if (!guild.getMembers().contains(player.getUniqueId())) {
                        event.setCancelled(true);
                        player.sendMessage("§cThis area is protected by " + guild.getName());
                    }
                });
    }
}