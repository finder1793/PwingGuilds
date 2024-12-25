package com.pwing.guilds.exp;

import com.pwing.guilds.PwingGuilds;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import org.bukkit.entity.Entity;
import org.bukkit.block.Block;

public class GuildExpManager {
    private final PwingGuilds plugin;
    private boolean mythicMobsEnabled;

    public GuildExpManager(PwingGuilds plugin) {
        this.plugin = plugin;
        setupMythicMobs();
    }

    private void setupMythicMobs() {
        if (plugin.getServer().getPluginManager().getPlugin("MythicMobs") != null) {
            mythicMobsEnabled = true;
        }
    }

    public long calculateBlockExp(Block block) {
        String blockType = block.getType().name();
        return plugin.getConfig().getLong("exp-sources.blocks.values." + blockType,
                plugin.getConfig().getLong("exp-sources.blocks.values.DEFAULT", 1));
    }

    public long calculateMobExp(Entity entity) {
        if (mythicMobsEnabled && isMythicMob(entity)) {
            return calculateMythicMobExp(entity);
        }
        
        String entityType = entity.getType().name();
        return plugin.getConfig().getLong("exp-sources.vanilla-mobs.values." + entityType,
                plugin.getConfig().getLong("exp-sources.vanilla-mobs.values.DEFAULT", 5));
    }

    private boolean isMythicMob(Entity entity) {
        return MythicBukkit.inst().getMobManager().isActiveMob(entity.getUniqueId());
    }

    private long calculateMythicMobExp(Entity entity) {
        ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getMythicMobInstance(entity);
        String mobId = mythicMob.getMobType();
        
        // Check for specific mob ID first
        long specificExp = plugin.getConfig().getLong("exp-sources.mythicmobs.mobs." + mobId, -1);
        if (specificExp != -1) {
            return specificExp;
        }
        
        // Fall back to tier multiplier
        String tier = mythicMob.getType().getConfig().getString("tier", "DEFAULT");
        return plugin.getConfig().getLong("exp-sources.mythicmobs.multipliers." + tier,
                plugin.getConfig().getLong("exp-sources.mythicmobs.multipliers.DEFAULT", 10));
    }
}