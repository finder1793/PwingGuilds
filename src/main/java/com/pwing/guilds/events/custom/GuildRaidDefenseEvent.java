package com.pwing.guilds.events.custom;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.ChunkLocation;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.attribute.Attribute;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

public class GuildRaidDefenseEvent extends GuildEvent implements Listener {
    private final Map<Guild, Integer> wavesDefended = new HashMap<>();
    private final Map<Guild, Integer> mobKills = new HashMap<>();
    private final Map<UUID, LivingEntity> spawnedMobs = new HashMap<>();
    private int currentWave = 0;
    private final Random random = new Random();
    private BukkitTask waveTask;

    public GuildRaidDefenseEvent(PwingGuilds plugin, String name, int duration) {
        super(plugin, name, duration);
        this.description = "Defend against waves of mobs attacking guild territories!";
    }

    @Override
    public void start() {
        isActive = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.broadcastMessage("§4§lRaid Defense has begun! Protect your territories!");
        startNextWave();
    }

    @Override
    public void updateScores() {
        wavesDefended.forEach((guild, waves) -> {
            int mobsKilled = calculateMobsKilled(guild);
            int score = (waves * 100) + mobsKilled;
            scores.put(guild, score);
        });
    }

    private void startNextWave() {
        currentWave++;
        Bukkit.broadcastMessage("§c§lWave " + currentWave + " incoming!");

        plugin.getGuildManager().getGuilds().forEach(guild -> {
            int mobCount = 5 + (currentWave * 2);
            spawnMobsAtGuild(guild, mobCount);
        });

        waveTask = Bukkit.getScheduler().runTaskLater(plugin, 
            this::startNextWave, 20 * 60 * 2);
    }

    private void spawnMobsAtGuild(Guild guild, int count) {
        Set<ChunkLocation> claims = guild.getClaimedChunks();
        if (claims.isEmpty()) return;

        ChunkLocation claim = (ChunkLocation) claims.toArray()[random.nextInt(claims.size())];
        Location spawnLoc = claim.toLocation().add(8, 1, 8);

        for (int i = 0; i < count; i++) {
            EntityType type = getRandomMobType();
            LivingEntity mob = (LivingEntity) spawnLoc.getWorld().spawnEntity(spawnLoc, type);
            mob.setCustomName("§c§lRaid Mob - Wave " + currentWave);
            mob.setCustomNameVisible(true);
            
            double health = 20 * (1 + (currentWave * 0.5));
            mob.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(health);
            mob.setHealth(health);
            
            spawnedMobs.put(mob.getUniqueId(), mob);
        }
    }

    private EntityType getRandomMobType() {
        return switch (random.nextInt(5)) {
            case 0 -> EntityType.ZOMBIE;
            case 1 -> EntityType.SKELETON;
            case 2 -> EntityType.SPIDER;
            case 3 -> EntityType.WITCH;
            default -> EntityType.CREEPER;
        };
    }

    @EventHandler
    public void onMobDeath(EntityDeathEvent event) {
        if (!isActive || !spawnedMobs.containsKey(event.getEntity().getUniqueId())) return;

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            plugin.getGuildManager().getPlayerGuild(killer.getUniqueId()).ifPresent(guild -> {
                mobKills.merge(guild, 1, Integer::sum);
                killer.sendMessage("§a+1 Raid Mob Kill for " + guild.getName());
            });
        }
        spawnedMobs.remove(event.getEntity().getUniqueId());
    }

    @Override
    public void end() {
        if (waveTask != null) {
            waveTask.cancel();
        }
        spawnedMobs.values().forEach(Entity::remove);
        super.end();
    }

    private int calculateMobsKilled(Guild guild) {
        return mobKills.getOrDefault(guild, 0);
    }
}

