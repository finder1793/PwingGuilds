package com.pwing.guilds.util;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import com.pwing.guilds.PwingGuilds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private Player targetPlayer;
    private final PwingGuilds plugin;

    public ItemBuilder(Material material, PwingGuilds plugin) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
        this.plugin = plugin;
    }

    public ItemBuilder name(String name) {
        // Translate color codes
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    public ItemBuilder lore(String... lore) {
        // Translate color codes
        meta.setLore(Arrays.stream(lore)
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder lore(List<String> lore) {
        // Translate color codes
        meta.setLore(lore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList()));
        return this;
    }

    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    public ItemBuilder skullOwner(OfflinePlayer player) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    public ItemBuilder forPlayer(Player player) {
        this.targetPlayer = player;
        return this;
    }

    public ItemBuilder modelData(int data) {
        // Only attempt to set model data if the feature is enabled
        if (plugin.getItemCompatHandler().isEnabled()) {
            plugin.getItemCompatHandler().setCustomModelData(meta, data, targetPlayer);
        }
        return this;
    }

    // Static convenience method for config items with version fallbacks
    public static ItemBuilder fromConfig(String path, PwingGuilds plugin, Player player) {
        boolean useModelData = plugin.getItemCompatHandler().isEnabled();
        
        // Get appropriate material
        String material;
        if (useModelData) {
            material = plugin.getConfig().getString("gui.items." + path + ".material");
        } else {
            material = plugin.getConfig().getString("advanced.custom-model-data.fallback-materials." + path);
        }
        
        ItemBuilder builder = new ItemBuilder(Material.valueOf(material), plugin)
            .forPlayer(player);
        
        // Only set model data if feature is enabled
        if (useModelData) {
            int modelData = plugin.getConfig().getInt("gui.items." + path + ".model-data", 0);
            if (modelData > 0) {
                builder.modelData(modelData);
            }
        }
        
        return builder;
    }

    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
