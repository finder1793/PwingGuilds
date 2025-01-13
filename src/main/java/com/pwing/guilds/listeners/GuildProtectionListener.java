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

/**
 * Listener for guild protection-related events.
 */
public class GuildProtectionListener implements Listener {
    private final PwingGuilds plugin;

    /**
     * Constructs a new GuildProtectionListener.
     * @param plugin The plugin instance.
     */
    public GuildProtectionListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles block break events.
     * @param event The block break event.
     */
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

    /**
     * Handles block place events.
     * @param event The block place event.
     */
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

    /**
     * Handles player interact events.
     * @param event The player interact event.
     */
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

    /**
     * Handles bucket empty events.
     * @param event The bucket empty event.
     */
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

    /**
     * Handles bucket fill events.
     * @param event The bucket fill event.
     */
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

    /**
     * Handles entity damage events.
     * @param event The entity damage event.
     */
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

    /**
     * Handles hanging break events.
     * @param event The hanging break event.
     */
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