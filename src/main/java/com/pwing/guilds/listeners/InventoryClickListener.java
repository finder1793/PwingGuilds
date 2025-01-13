package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.gui.StructureManagementGUI;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Listens for inventory click events involving guild storage.
 */
public class InventoryClickListener implements Listener {
    private final PwingGuilds plugin;

    /**
     * Constructs a new InventoryClickListener.
     * @param plugin The PwingGuilds plugin instance.
     */
    public InventoryClickListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles inventory click events.
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = plugin.getMessageManager().getMessage("gui.titles.structures");
        if (event.getView().getTitle().equals(title)) {
            plugin.getGuildManager().getPlayerGuild(event.getWhoClicked().getUniqueId()).ifPresent(guild -> {
                new StructureManagementGUI(plugin).handleInventoryClick(event, guild);
            });
        }
    }
}
