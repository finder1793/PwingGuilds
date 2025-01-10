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

/**
 * Represents a guild buff that can be purchased and applied to guild members.
 * Buffs can provide potion effects, stat modifications via MythicMobs, or both.
 * Each buff has an associated cost, duration, and permission requirement.
 */
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

    /**
     * Defines the type of buff effect
     */
    public enum BuffType {
        /** Potion effect only */
        POTION,
        /** Stat boost only */
        STAT,
        /** Both potion and stat effects */
        BOTH
    }

    private final Material material;
    private final int slot;

    /**
     * Creates a new guild buff
     * @param plugin Plugin instance
     * @param name Buff name
     * @param potionEffect Potion effect type
     * @param statType Stat type
     * @param statValue Stat value
     * @param type Buff type
     * @param duration Duration in seconds
     * @param cost Cost to activate
     * @param material GUI material
     * @param slot GUI slot
     * @param permission Permission required
     */
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

    /**
     * Applies this buff to a player
     * @param player The player to receive the buff
     * @throws IllegalStateException if MythicMobs integration fails for stat buffs
     */
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

    /**
     * Controls if this buff's effects should be removed when plugin reloads
     * @return true to remove on reload, false to persist
     */
    @Override
    public boolean removeOnReload() {
        return true;
    }

    // Getters with simple documentation
    /** @return The buff's name */
    public String getName() { return name; }
    
    /** @return The buff's potion effect type */
    public PotionEffectType getPotionEffect() { return potionEffect; }
    
    /** @return The buff's cost */
    public int getCost() { return cost; }

    public StatType getStatType() { return statType; }
    public double getStatValue() { return statValue; }
    public int getLevel() { return level; }
    public int getDuration() { return duration; }

    /**
     * Gets the permission required to purchase/use this buff
     * @return Permission node string
     */
    public String getPermission() { return permission; }

    /**
     * Gets the material used to represent this buff in GUIs
     * @return Bukkit Material enum value
     */
    public Material getMaterial() { return material; }
    public int getSlot() { return slot; }
    public BuffType getType() { return type; }
}