package com.pwing.guilds.buffs;

import com.pwing.guilds.PwingGuilds;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.StatModifierType;
import io.lumine.mythic.core.skills.stats.StatType;
import io.lumine.mythic.core.skills.stats.StatRegistry;
import io.lumine.mythic.core.skills.stats.StatSource;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildBuff implements StatSource {
    private final PwingGuilds plugin;
    private final String name;
    private final PotionEffectType potionEffect;
    private final StatType statType;
    private final double statValue;
    private final int level;
    private final int cost;
    private final int duration;
    private final String permission;
    private final BuffType type;

    public enum BuffType {
        POTION,
        STAT,
        BOTH
    }

    private final Material material;
    private final int slot;

    public GuildBuff(PwingGuilds plugin, String name, PotionEffectType potionEffect, StatType statType, 
                    double statValue, int level, int cost, int duration, String permission, 
                    BuffType type, Material material, int slot) {
        this.plugin = plugin;
        this.name = name;
        this.potionEffect = potionEffect;
        this.statType = statType;
        this.statValue = statValue;
        this.level = level;
        this.cost = cost;
        this.duration = duration;
        this.permission = permission;
        this.type = type;
        this.material = material;
        this.slot = slot;
    }

    private final Map<UUID, Long> buffExpirations = new HashMap<>();

    public void applyToMember(Player player) {
        // Potion effects work independently
        if (potionEffect != null) {
            player.addPotionEffect(new PotionEffect(potionEffect, duration * 20, level - 1));
        }
        // MythicMobs stats only run if plugin is present
        if (statType != null && Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
            try {
                StatRegistry playerStatRegistry = MythicBukkit.inst().getPlayerManager()
                    .getProfile(player)
                    .getStatRegistry();
                    
                playerStatRegistry.putValueSilently(
                    statType, 
                    this,  // Using GuildBuff as StatSource
                    StatModifierType.ADDITIVE,
                    statValue
                );
                
                playerStatRegistry.updateDirtyStats();
                
                // Schedule removal
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    playerStatRegistry.removeValueSilently(statType, this);
                    playerStatRegistry.updateDirtyStats();
                }, duration * 20L);
            } catch (Exception ignored) {}
        }
    }

    @Override
    public boolean removeOnReload() {
        return true;
    }

    public String getName() { return name; }
    public PotionEffectType getPotionEffect() { return potionEffect; }
    public StatType getStatType() { return statType; }
    public double getStatValue() { return statValue; }
    public int getLevel() { return level; }
    public int getCost() { return cost; }
    public int getDuration() { return duration; }
    public String getPermission() { return permission; }
    public BuffType getType() { return type; }
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
}