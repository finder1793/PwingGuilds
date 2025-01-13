package com.pwing.guilds.gui;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public class StructureManagementGUI {
    private final PwingGuilds plugin;

    public StructureManagementGUI(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    public void openStructureMenu(Player player, Guild guild) {
        String title = plugin.getMessageManager().getMessage("gui.titles.structures");
        int size = 27;

        Inventory inv = Bukkit.createInventory(null, size, title);

        ConfigurationSection structuresSection = plugin.getStructuresConfig().getConfigurationSection("structures");
        if (structuresSection != null) {
            int slot = 0;
            for (String structureName : structuresSection.getKeys(false)) {
                ConfigurationSection structureSection = structuresSection.getConfigurationSection(structureName);
                if (structureSection == null) continue;

                ItemStack item = new ItemStack(Material.PAPER);
                ItemMeta meta = item.getItemMeta();
                meta.setDisplayName(structureName);
                meta.setLore(List.of("Cost: " + structureSection.getConfigurationSection("cost").getKeys(false).toString()));
                item.setItemMeta(meta);

                inv.setItem(slot++, item);
            }
        }

        player.openInventory(inv);
    }

    public void handleInventoryClick(InventoryClickEvent event, Guild guild) {
        String title = plugin.getMessageManager().getMessage("gui.titles.structures");

        if (!event.getView().getTitle().equals(title)) return;

        event.setCancelled(true);
        if (!(event.getWhoClicked() instanceof Player player)) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || !clickedItem.hasItemMeta()) return;

        String structureName = clickedItem.getItemMeta().getDisplayName();
        if (plugin.getGuildManager().canPasteSchematic(guild, player, structureName)) {
            plugin.getGuildManager().pasteSchematic(guild, player, structureName);
            player.sendMessage(plugin.getMessageManager().getMessage("guild.structure-built"));
        }
    }
}
