package com.pwing.guilds.perks;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.api.GuildPerkActivateEvent;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Manages guild perks.
 */
public class GuildPerks {
    private final int memberLimit;
    private final int teleportCooldown;
    private final double expMultiplier;
    private final boolean keepInventory;
    private final int homeLimit;
    private final PwingGuilds plugin;
    private final int level;
    private final Guild guild;
    private final int storageRows;
    private final boolean storageAccess;

    /**
     * Constructs a new GuildPerks instance.
     * @param plugin The plugin instance.
     * @param guild The guild.
     * @param level The level of the guild.
     */
    public GuildPerks(PwingGuilds plugin, Guild guild, int level) {
        this.plugin = plugin;
        this.guild = guild;
        this.level = level;

        // Get default values if perks section is null
        ConfigurationSection perks = plugin.getConfig().getConfigurationSection("guild-levels." + level + ".perks");
        if (perks == null) {
            // Set default values
            this.memberLimit = 5;
            this.teleportCooldown = 300;
            this.expMultiplier = 1.0;
            this.keepInventory = false;
            this.homeLimit = 1;
            this.storageRows = 1;
            this.storageAccess = true;
        } else {
            // Load from config
            this.memberLimit = perks.getInt("member-limit", 5);
            this.teleportCooldown = perks.getInt("teleport-cooldown", 300);
            this.expMultiplier = perks.getDouble("exp-multiplier", 1.0);
            this.keepInventory = perks.getBoolean("keep-inventory", false);
            this.homeLimit = perks.getInt("home-limit", 1);
            this.storageRows = perks.getInt("storage-rows", 1);
            this.storageAccess = perks.getBoolean("storage-access", true);
        }
    }

    /**
     * Activates a perk for the guild.
     * @param perkName The name of the perk to activate.
     * @return true if the perk was activated, false otherwise.
     */
    public boolean activatePerk(String perkName) {
        GuildPerkActivateEvent event = new GuildPerkActivateEvent(guild, perkName);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            switch (perkName.toLowerCase()) {
                case "keep-inventory":
                    return keepInventory;
                case "extra-homes":
                    return homeLimit > 1;
                case "exp-boost":
                    return expMultiplier > 1.0;
                case "guild-storage":
                    return storageRows > 0;
                default:
                    return false;
            }
        }
        return false;
    }

    /**
     * Gets the member limit for the guild.
     * @return The member limit.
     */
    public int getMemberLimit() {
        return memberLimit;
    }

    /**
     * Gets the teleport cooldown for the guild.
     * @return The teleport cooldown.
     */
    public int getTeleportCooldown() {
        return teleportCooldown;
    }

    /**
     * Gets the experience multiplier for the guild.
     * @return The experience multiplier.
     */
    public double getExpMultiplier() {
        return expMultiplier;
    }

    /**
     * Checks if the guild has keep inventory enabled.
     * @return true if keep inventory is enabled, false otherwise.
     */
    public boolean hasKeepInventory() {
        return keepInventory;
    }

    /**
     * Gets the home limit for the guild.
     * @return The home limit.
     */
    public int getHomeLimit() {
        return homeLimit;
    }

    /**
     * Gets the storage rows for the guild.
     * @return The storage rows.
     */
    public int getStorageRows() {
        return storageRows;
    }
}


