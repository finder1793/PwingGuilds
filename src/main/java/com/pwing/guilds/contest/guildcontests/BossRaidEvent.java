package com.pwing.guilds.contest.guildcontests;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.attribute.Attribute;
import java.util.HashMap;
import java.util.Map;
import com.pwing.guilds.guild.Guild;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;

/**
 * Represents a boss raid event that guilds can participate in
 */
public class BossRaidEvent extends GuildEvent implements Listener {
    private LivingEntity boss;
    private ActiveMob mythicBoss;
    private final Map<Guild, Double> damageDealt = new HashMap<>();
    private final boolean isMythicMob;
    private final String bossType;

    /**
     * Creates a new boss raid event
     * @param plugin Plugin instance
     * @param name Name of the event
     * @param duration Duration in seconds
     */
    public BossRaidEvent(PwingGuilds plugin, String name, int duration) {
        super(plugin, name, duration);
        this.description = "Work together to defeat the mighty boss! Most damage dealt wins!";
        this.bossType = plugin.getConfig().getString("events.boss-raid.boss-type", "WITHER");
        this.isMythicMob = bossType.startsWith("mythic:");
    }

    @Override
    public void start() {
        isActive = true;
        spawnBoss();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.broadcastMessage("§4§lBoss Raid has begun! Defeat the boss for glory!");
    }

    @Override
    public void end() {
        isActive = false;
        if (boss != null && !boss.isDead()) {
            boss.remove();
        }
        HandlerList.unregisterAll(this);
        updateScores();
        announceResults();
    }

    @Override
    public void updateScores() {
        damageDealt.forEach((guild, damage) ->
            scores.put(guild, (int) (damage * 10)));
    }

    private void spawnBoss() {
        World world = Bukkit.getWorlds().get(0);
        Location spawn = world.getSpawnLocation();

        if (isMythicMob) {
            String mythicType = bossType.substring(7); // Remove "mythic:" prefix
            mythicBoss = MythicBukkit.inst().getMobManager().spawnMob(mythicType, spawn);
            if (mythicBoss != null) {
                boss = (LivingEntity) mythicBoss.getEntity().getBukkitEntity();
            }
        } else {
            boss = (LivingEntity) world.spawnEntity(spawn, EntityType.valueOf(bossType));
            boss.setCustomName("§4§lGuild Raid Boss");
            boss.setCustomNameVisible(true);

            // Set vanilla boss attributes
            boss.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(1000.0);
            boss.setHealth(1000.0);
            boss.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).setBaseValue(20.0);
            boss.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(10.0);
        }
    }

    /**
     * Handles damage dealt to the boss
     * @param event The damage event
     */
    @EventHandler
    public void onBossDamage(EntityDamageByEntityEvent event) {
        if (!isActive || event.getEntity() != boss) return;

        Player damager = null;
        if (event.getDamager() instanceof Player) {
            damager = (Player) event.getDamager();
        } else if (event.getDamager() instanceof Projectile) {
            Projectile projectile = (Projectile) event.getDamager();
            if (projectile.getShooter() instanceof Player) {
                damager = (Player) projectile.getShooter();
            }
        }

        if (damager != null) {
            final double finalDamage = event.getFinalDamage();
            final Player finalDamager = damager;
            plugin.getGuildManager().getPlayerGuild(finalDamager.getUniqueId()).ifPresent(guild -> {
                damageDealt.merge(guild, finalDamage, Double::sum);
                finalDamager.sendMessage("§c+" + String.format("%.1f", finalDamage) + " damage to Boss!");
            });
        }
    }

    /**
     * Handles the boss death event
     * @param event The death event
     */
    @EventHandler
    public void onBossDeath(EntityDeathEvent event) {
        if (!isActive || event.getEntity() != boss) return;

        Bukkit.broadcastMessage("§4§lThe Raid Boss has been defeated!");
        end();
    }

    @Override
    public void announceResults() {
        Bukkit.broadcastMessage("§4§lBoss Raid Results:");
        scores.entrySet().stream()
            .sorted(Map.Entry.<Guild, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                double damage = damageDealt.get(entry.getKey());
                Bukkit.broadcastMessage("§e" + entry.getKey().getName() +
                    " §7- §c" + String.format("%.1f", damage) + " damage dealt");
            });
    }
}



