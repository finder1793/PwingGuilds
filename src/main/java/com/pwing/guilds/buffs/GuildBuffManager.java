package com.pwing.guilds.buffs;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.message.MessageManager;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.skills.stats.StatExecutor;
import io.lumine.mythic.core.skills.stats.StatType;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.entity.Player;
import io.lumine.mythic.core.skills.stats.StatRegistry;
import java.util.*;
import java.util.UUID;
import java.util.Map;
import java.util.HashMap;

/**
 * Manages guild buff system including loading, activation and expiration.
 * Handles buff effects, costs, and player application.
 */
public class GuildBuffManager {
    private final Map<String, GuildBuff> availableBuffs = new HashMap<>();
    private final Map<UUID, Map<String, GuildBuff>> activeBuffs = new HashMap<>();
    private final PwingGuilds plugin;

    /**
     * Creates a new GuildBuffManager instance
     * @param plugin The plugin instance
     */
    public GuildBuffManager(PwingGuilds plugin) {
        this.plugin = plugin;
        loadBuffs();
    }

    private void loadBuffs() {
        FileConfiguration buffsConfig = plugin.getConfigManager().getConfig("buffs.yml");
        if (buffsConfig == null) return;

        ConfigurationSection buffsSection = buffsConfig.getConfigurationSection("buffs");
        if (buffsSection == null) return;

        for (String key : buffsSection.getKeys(false)) {
            ConfigurationSection buffSection = buffsSection.getConfigurationSection(key);
            if (buffSection == null) continue;

            String effectName = buffSection.getString("effect");
            PotionEffectType potionEffect = null;
            if (effectName != null) {
                potionEffect = PotionEffectType.getByName(effectName);
            }

            StatType statType = null;
            String statName = buffSection.getString("stat-type");
            if (statName != null && Bukkit.getPluginManager().isPluginEnabled("MythicMobs")) {
                try {
                    StatExecutor statManager = MythicBukkit.inst().getStatManager();
                    Optional<StatType> stat = statManager.getStat(statName);
                    statType = stat.orElse(null);
                } catch (Exception ignored) {}
            }

            Material material = Material.valueOf(buffSection.getString("material", "BARRIER"));
            int slot = buffSection.getInt("slot", 0);

            GuildBuff buff = new GuildBuff(
                plugin,
                buffSection.getString("name"),
                potionEffect,
                statType,
                buffSection.getDouble("stat-value", 0.0),
                buffSection.getInt("level", 1),
                buffSection.getInt("cost", 1000),
                buffSection.getInt("duration", 3600),
                buffSection.getString("permission", ""),
                GuildBuff.BuffType.valueOf(buffSection.getString("type", "POTION").toUpperCase()),
                material,
                slot
            );

            availableBuffs.put(key, buff);
        }
    }

    /**
     * Activates a buff for a guild
     * @param guild The guild to activate the buff for
     * @param buffName The name of the buff to activate
     * @return true if activation was successful
     */
    public boolean activateGuildBuff(Guild guild, String buffName) {
        GuildBuff buff = availableBuffs.get(buffName);
        if (buff == null) return false;

        // Get the player attempting to activate the buff
        Player activator = guild.getMembers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .findFirst()
            .orElse(null);

        if (activator == null) return false;

        double cost = buff.getCost();

        if (!activator.hasPermission("guild.buff." + buffName)) {
            activator.sendMessage(plugin.getMessageManager().getMessage("buffs.no-permission")); 
            return false;
        }

        if (!guild.hasMoney(cost)) {
            activator.sendMessage(plugin.getMessageManager().getMessage("buffs.not-enough-money")
                .replace("%cost%", String.format("%.2f", cost)));
            return false;
        }

        if (!guild.withdrawMoney(cost)) {
            activator.sendMessage(plugin.getMessageManager().getMessage("buffs.not-enough-money")
                .replace("%cost%", String.format("%.2f", cost)));
            return false;
        }

        guild.getMembers().stream()
            .map(Bukkit::getPlayer)
            .filter(Objects::nonNull)
            .forEach(p -> {
                buff.applyToMember(p);
                p.sendMessage(plugin.getMessageManager().getMessage("buffs.activated")
                    .replace("%buff%", buff.getName()));
            });

        return true;
    }

    /**
     * Gets all available guild buffs
     * @return Map of buff names to buff objects
     */
    public Map<String, GuildBuff> getAvailableBuffs() {
        return availableBuffs;
    }

    public void removeExpiredBuffs() {
        for (Map.Entry<UUID, Map<String, GuildBuff>> entry : activeBuffs.entrySet()) {
            Player player = Bukkit.getPlayer(entry.getKey());
            if (player != null) {
                for (GuildBuff buff : entry.getValue().values()) {
                    StatRegistry registry = MythicBukkit.inst().getPlayerManager()
                        .getProfile(player)
                        .getStatRegistry();
                        
                    registry.removeValueSilently(buff.getStatType(), buff);
                    player.sendMessage(plugin.getMessageManager().getMessage("buffs.expired")
                        .replace("%buff%", buff.getName()));
                }
            }
        }
    }

    public void trackBuff(UUID playerId, String buffId, GuildBuff buff) {
        activeBuffs.computeIfAbsent(playerId, k -> new HashMap<>()).put(buffId, buff);
    }

    public void removeBuff(UUID playerId, String buffId) {
        if (activeBuffs.containsKey(playerId)) {
            activeBuffs.get(playerId).remove(buffId);
        }
    }
}