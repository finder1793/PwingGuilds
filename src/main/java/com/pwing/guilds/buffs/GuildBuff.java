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
 * Represents a temporary buff/bonus that can be activated by guilds.
 * Buffs can provide potion effects or stat boosts to guild members.
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
     * Creates a new guild buff.
     * @param plugin Plugin instance.
     * @param name Buff name.
     * @param potionEffect Potion effect type.
     * @param statType Stat type.
     * @param statValue Stat value.
     * @param level Buff level.
     * @param cost Cost to activate.
     * @param duration Duration in seconds.
     * @param permission Permission required.
     * @param type Buff type.
     * @param material GUI material.
     * @param slot GUI slot.
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
    /**
     * Gets the name of the buff.
     * @return The buff's name
     */
    public String getName() { return name; }
    
    /**
     * Gets the potion effect type of the buff.
     * @return The buff's potion effect type
     */
    public PotionEffectType getPotionEffect() { return potionEffect; }
    
    /**
     * Gets the cost to activate the buff.
     * @return The buff's cost
     */
    public int getCost() { return cost; }

    /**
     * Gets the stat type affected by this buff
     * @return The stat type
     */
    public StatType getStatType() { return statType; }

    /**
     * Gets the value of the stat boost provided by this buff
     * @return The stat value
     */
    public double getStatValue() { return statValue; }

    /**
     * Gets the level/tier of this buff
     * @return The buff level
     */
    public int getLevel() { return level; }

    /**
     * Gets the duration of this buff in seconds
     * @return Duration in seconds
     */
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
    
    /**
     * Gets the slot in the GUI where the buff is displayed.
     * @return The buff's slot.
     */
    public int getSlot() { return slot; }
    
    /**
     * Gets the type of the buff.
     * @return The buff's type.
     */
    public BuffType getType() { return type; }
}