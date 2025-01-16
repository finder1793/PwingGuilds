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

/**
 * Utility class for building custom items.
 */
public class ItemBuilder {
    private final ItemStack item;
    private final ItemMeta meta;
    private Player targetPlayer;
    private final PwingGuilds plugin;

    /**
     * Constructs a new ItemBuilder.
     * @param material The material of the item.
     * @param plugin The PwingGuilds plugin instance.
     */
    public ItemBuilder(Material material, PwingGuilds plugin) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
        this.plugin = plugin;
    }

    /**
     * Sets the name of the item.
     * @param name The name to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder name(String name) {
        // Translate color codes
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        return this;
    }

    /**
     * Sets the lore of the item.
     * @param lore The lore to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder lore(String... lore) {
        // Translate color codes
        meta.setLore(Arrays.stream(lore)
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList()));
        return this;
    }

    /**
     * Sets the lore of the item.
     * @param lore The lore to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder lore(List<String> lore) {
        // Translate color codes
        meta.setLore(lore.stream()
                .map(line -> ChatColor.translateAlternateColorCodes('&', line))
                .collect(Collectors.toList()));
        return this;
    }

    /**
     * Sets the amount of the item.
     * @param amount The amount to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder amount(int amount) {
        item.setAmount(amount);
        return this;
    }

    /**
     * Sets the skull owner of the item.
     * @param player The OfflinePlayer whose skull to use.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder skullOwner(OfflinePlayer player) {
        if (meta instanceof SkullMeta skullMeta) {
            skullMeta.setOwningPlayer(player);
        }
        return this;
    }

    /**
     * Sets the player for whom the item is being built.
     * @param player The player.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder forPlayer(Player player) {
        this.targetPlayer = player;
        return this;
    }

    /**
     * Sets the custom model data of the item.
     * @param data The model data to set.
     * @return The ItemBuilder instance.
     */
    public ItemBuilder modelData(int data) {
        // Only attempt to set model data if the feature is enabled
        if (plugin.getItemCompatHandler().isEnabled()) {
            plugin.getItemCompatHandler().setCustomModelData(meta, data, targetPlayer);
        }
        return this;
    }

    /**
     * Creates an ItemBuilder from a configuration path.
     * @param path The configuration path.
     * @param plugin The PwingGuilds plugin instance.
     * @param player The player for whom the item is being built.
     * @return The ItemBuilder instance.
     */
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
        
        // Set name and lore from gui.yml
        String name = plugin.getConfig().getString("gui.items." + path + ".name");
        List<String> lore = plugin.getConfig().getStringList("gui.items." + path + ".lore");
        builder.name(name).lore(lore);
        
        return builder;
    }

    /**
     * Builds the item.
     * @return The built ItemStack.
     */
    public ItemStack build() {
        item.setItemMeta(meta);
        return item;
    }
}
