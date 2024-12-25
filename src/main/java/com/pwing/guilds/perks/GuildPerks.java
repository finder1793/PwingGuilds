package com.pwing.guilds.perks;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.configuration.ConfigurationSection;

public class GuildPerks {
    private final int memberLimit;
    private final int teleportCooldown;
    private final double expMultiplier;
    private final boolean keepInventory;
    private final int homeLimit;
    private final PwingGuilds plugin;
    private final int level;

    public GuildPerks(PwingGuilds plugin, int level) {
        this.plugin = plugin;
        this.level = level;
        ConfigurationSection perks = plugin.getConfig().getConfigurationSection("guild-levels." + level + ".perks");
        this.memberLimit = perks.getInt("member-limit", 5);
        this.teleportCooldown = perks.getInt("teleport-cooldown", 300);
        this.expMultiplier = perks.getDouble("exp-multiplier", 1.0);
        this.keepInventory = perks.getBoolean("keep-inventory", false);
        this.homeLimit = perks.getInt("home-limit", 1);
    }

    public int getMemberLimit() { return memberLimit; }
    public int getTeleportCooldown() { return teleportCooldown; }
    public double getExpMultiplier() { return expMultiplier; }
    public boolean hasKeepInventory() { return keepInventory; }
    public int getHomeLimit() { return homeLimit; }
}