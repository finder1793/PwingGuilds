package com.pwing.guilds.compat;

import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.entity.Player;

/**
 * Optional handler for version-specific item modifications
 */
public class ItemCompatibilityHandler {
    private final Plugin plugin;
    private final boolean enabled;
    private final boolean hasViaVersion;

    /**
     * Constructs a new ItemCompatibilityHandler.
     * @param plugin The plugin instance.
     */
    public ItemCompatibilityHandler(Plugin plugin) {
        this.plugin = plugin;
        // CustomModelData is disabled by default
        this.enabled = plugin.getConfig().getBoolean("advanced.custom-model-data.enabled", false);
        // ViaVersion support is only checked if CustomModelData is enabled
        this.hasViaVersion = enabled && plugin.getServer().getPluginManager().getPlugin("ViaVersion") != null;
        
        if (enabled) {
            plugin.getLogger().info("Custom Model Data support enabled" + 
                (hasViaVersion ? " with ViaVersion support" : ""));
        }
    }

    /**
     * Sets custom model data for an item.
     * @param meta The item meta.
     * @param data The custom model data.
     * @param player The player.
     */
    public void setCustomModelData(ItemMeta meta, int data, Player player) {
        if (!enabled || data <= 0) return;

        try {
            // Only attempt ViaVersion check if it's present and player is specified
            if (hasViaVersion && player != null) {
                handleViaVersion(meta, data, player);
            } else {
                // Regular server implementation
                meta.setCustomModelData(data);
            }
        } catch (Exception e) {
            // Silently fail - custom model data is optional
        }
    }

    private void handleViaVersion(ItemMeta meta, int data, Player player) {
        try {
            int playerVersion = com.viaversion.viaversion.api.Via.getAPI()
                .getPlayerVersion(player.getUniqueId());
            
            if (playerVersion >= 765) { // 1.20.4+
                meta.setCustomModelData(data);
            }
            // If player version is lower, skip setting model data
        } catch (Exception e) {
            // Silently fail - ViaVersion support is optional
        }
    }

    /**
     * Checks if the compatibility handler is enabled.
     * @return true if enabled, false otherwise.
     */
    public boolean isEnabled() {
        return enabled;
    }
}
