package com.pwing.guilds.gui;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import com.pwing.guilds.guild.ChunkLocation;

public class GuildGUIListener implements Listener {
    private final PwingGuilds plugin;

    public GuildGUIListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        
        String title = event.getView().getTitle();
        if (!title.equals("Guild Management") && !title.equals("Guild Claims Map")) return;
        
        event.setCancelled(true);
        
        if (title.equals("Guild Claims Map")) {
            handleClaimsMapClick(event, player);
        } else {
            handleMainMenuClick(event, player);
        }
    }

    private void handleClaimsMapClick(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) return;
        
        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            String[] lore = event.getCurrentItem().getItemMeta().getLore().toArray(new String[0]);
            int x = Integer.parseInt(lore[0].substring(4));
            int z = Integer.parseInt(lore[1].substring(4));
            
            if (event.getCurrentItem().getType().name().contains("EMERALD")) {
                guild.unclaimChunk(new ChunkLocation(player.getWorld().getChunkAt(x, z)));
                player.sendMessage("§aChunk unclaimed!");
            } else {
                if (guild.canClaim()) {
                    guild.claimChunk(new ChunkLocation(player.getWorld().getChunkAt(x, z)));
                    player.sendMessage("§aChunk claimed!");
                } else {
                    player.sendMessage("§cYou've reached your maximum claim limit!");
                }
            }
            
            // Refresh the GUI
            new GuildManagementGUI(plugin).openClaimsMap(player, guild);
        });
    }

    private void handleMainMenuClick(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) return;
        
        switch (event.getSlot()) {
            case 11 -> player.sendMessage("§eMember management coming soon!");
            case 13 -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .ifPresent(guild -> new GuildManagementGUI(plugin).openClaimsMap(player, guild));
            case 15 -> player.sendMessage("§eGuild settings coming soon!");
        }
    }
}

