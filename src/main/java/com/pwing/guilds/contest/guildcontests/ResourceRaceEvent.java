package com.pwing.guilds.contest.guildcontests;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import com.pwing.guilds.guild.Guild;

public class ResourceRaceEvent extends GuildEvent implements Listener {
    private final Set<Material> targetMaterials;
    private final Map<Guild, Map<Material, Integer>> resourceCounts = new HashMap<>();

    public ResourceRaceEvent(PwingGuilds plugin, String name, int duration) {
        super(plugin, name, duration);
        this.description = "Mine the most valuable resources for your guild!";
        this.targetMaterials = Set.of(
            Material.DIAMOND_ORE,
            Material.ANCIENT_DEBRIS,
            Material.EMERALD_ORE,
            Material.GOLD_ORE
        );
    }

    @Override
    public void start() {
        isActive = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.broadcastMessage("§b§lResource Race has begun! Mine valuable resources for points!");
    }

    @Override
    public void end() {
        isActive = false;
        HandlerList.unregisterAll(this);
        updateScores();
        announceResults();
    }

    @Override
    public void updateScores() {
        resourceCounts.forEach((guild, materials) -> {
            int points = calculatePoints(materials);
            scores.put(guild, points);
        });
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!isActive) return;

        Material material = event.getBlock().getType();
        if (targetMaterials.contains(material)) {
            Player player = event.getPlayer();
            plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                resourceCounts.computeIfAbsent(guild, k -> new HashMap<>())
                    .merge(material, 1, Integer::sum);
                player.sendMessage("§a+1 " + formatMaterialName(material) + " for " + guild.getName());
            });
        }
    }

    private int calculatePoints(Map<Material, Integer> materials) {
        return materials.entrySet().stream()
            .mapToInt(entry -> getPointValue(entry.getKey()) * entry.getValue())
            .sum();
    }

    private int getPointValue(Material material) {
        return switch (material) {
            case ANCIENT_DEBRIS -> 10;
            case DIAMOND_ORE -> 5;
            case EMERALD_ORE -> 4;
            case GOLD_ORE -> 2;
            default -> 1;
        };
    }

    private String formatMaterialName(Material material) {
        return material.name()
            .replace("_", " ")
            .toLowerCase()
            .substring(0, 1)
            .toUpperCase() +
            material.name()
            .replace("_", " ")
            .toLowerCase()
            .substring(1);
    }

    @Override
    public void announceResults() {
        Bukkit.broadcastMessage("§b§lResource Race Results:");
        scores.entrySet().stream()
            .sorted(Map.Entry.<Guild, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry ->
                Bukkit.broadcastMessage("§e" + entry.getKey().getName() +
                    " §7- §6" + entry.getValue() + " points"));
    }
}
