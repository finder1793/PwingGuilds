package com.pwing.guilds.perks;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.api.GuildPerkActivateEvent;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

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

    public int getMemberLimit() {
        return memberLimit;
    }

    public int getTeleportCooldown() {
        return teleportCooldown;
    }

    public double getExpMultiplier() {
        return expMultiplier;
    }

    public boolean hasKeepInventory() {
        return keepInventory;
    }

    public int getHomeLimit() {
        return homeLimit;
    }

    public int getStorageRows() {
        return storageRows;
    }
}


