package com.pwing.guilds.buffs;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.Map;

public class GuildBuffManager {
    private final Map<String, GuildBuff> availableBuffs = new HashMap<>();
    private final PwingGuilds plugin;

    public GuildBuffManager(PwingGuilds plugin) {
        this.plugin = plugin;
        loadBuffs();
    }

    private void loadBuffs() {
        ConfigurationSection buffsSection = plugin.getConfig().getConfigurationSection("guild-buffs");
        if (buffsSection == null) return;

        for (String key : buffsSection.getKeys(false)) {
            ConfigurationSection buffSection = buffsSection.getConfigurationSection(key);
            if (buffSection == null) continue;

            String effectName = buffSection.getString("effect");
            PotionEffectType effectType = PotionEffectType.getByName(effectName);
            if (effectType == null) continue;

            GuildBuff buff = new GuildBuff(
                buffSection.getString("name"),
                effectType,
                buffSection.getInt("level", 1),
                buffSection.getInt("cost", 1000),
                buffSection.getInt("duration", 3600),
                buffSection.getString("permission", "")
            );

            availableBuffs.put(key, buff);
        }
    }

    public Map<String, GuildBuff> getAvailableBuffs() {
        return availableBuffs;
    }
}
