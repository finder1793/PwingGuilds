package com.pwing.guilds.perks;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.events.GuildPerkActivateEvent;
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

    public GuildPerks(PwingGuilds plugin, Guild guild, int level) {
        this.plugin = plugin;
        this.guild = guild;
        this.level = level;
        ConfigurationSection perks = plugin.getConfig().getConfigurationSection("guild-levels." + level + ".perks");
        this.memberLimit = perks.getInt("member-limit", 5);
        this.teleportCooldown = perks.getInt("teleport-cooldown", 300);
        this.expMultiplier = perks.getDouble("exp-multiplier", 1.0);
        this.keepInventory = perks.getBoolean("keep-inventory", false);
        this.homeLimit = perks.getInt("home-limit", 1);
    }

    public boolean activatePerk(String perkName) {
        GuildPerkActivateEvent event = new GuildPerkActivateEvent(guild, perkName);
        Bukkit.getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
            switch(perkName.toLowerCase()) {
                case "keep-inventory":
                    return keepInventory;
                case "extra-homes":
                    return homeLimit > 1;
                case "exp-boost":
                    return expMultiplier > 1.0;
                default:
                    return false;
            }
        }
        return false;
    }

    public int getMemberLimit() { return memberLimit; }
    public int getTeleportCooldown() { return teleportCooldown; }
    public double getExpMultiplier() { return expMultiplier; }
    public boolean hasKeepInventory() { return keepInventory; }
    public int getHomeLimit() { return homeLimit; }
}