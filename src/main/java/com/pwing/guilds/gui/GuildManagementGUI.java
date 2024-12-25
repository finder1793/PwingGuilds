package com.pwing.guilds.gui;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class GuildManagementGUI {
    private final PwingGuilds plugin;

    public GuildManagementGUI(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Guild Management");
        
        // Members Management
        ItemStack members = createItem(Material.PLAYER_HEAD, "§6Members", "§7Click to manage guild members");
        inv.setItem(11, members);
        
        // Claims Management
        ItemStack claims = createItem(Material.MAP, "§6Claims Map", "§7View and manage claimed chunks");
        inv.setItem(13, claims);
        
        // Guild Settings
        ItemStack settings = createItem(Material.COMPARATOR, "§6Guild Settings", "§7Configure guild settings");
        inv.setItem(15, settings);
        
        player.openInventory(inv);
    }

    public void openClaimsMap(Player player, Guild guild) {
        Inventory inv = Bukkit.createInventory(null, 54, "Guild Claims Map");
        
        int centerX = player.getLocation().getChunk().getX();
        int centerZ = player.getLocation().getChunk().getZ();
        
        for (int z = -4; z <= 4; z++) {
            for (int x = -4; x <= 4; x++) {
                int slot = (z + 4) * 9 + (x + 4);
                int chunkX = centerX + x;
                int chunkZ = centerZ + z;
                
                ItemStack chunkItem;
                if (guild.isChunkClaimed(player.getWorld().getChunkAt(chunkX, chunkZ))) {
                    chunkItem = createItem(Material.EMERALD_BLOCK, 
                        "§aClaimed Chunk", 
                        "§7X: " + chunkX,
                        "§7Z: " + chunkZ,
                        "",
                        "§eClick to unclaim");
                } else {
                    chunkItem = createItem(Material.GRASS_BLOCK,
                        "§7Unclaimed Chunk",
                        "§7X: " + chunkX,
                        "§7Z: " + chunkZ,
                        "",
                        "§eClick to claim");
                }
                
                inv.setItem(slot, chunkItem);
            }
        }
        
        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }
}
