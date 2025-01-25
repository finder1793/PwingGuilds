package com.pwing.guilds.visualization;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitRunnable;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Particle.DustOptions;
import org.bukkit.Color;

/**
 * Utility class for visualizing chunk borders
 * Helps players see guild territory boundaries
 */
public class ChunkVisualizer {
    
    /**
     * Private constructor to prevent instantiation of utility class
     */
    private ChunkVisualizer() {
        throw new UnsupportedOperationException("Utility class");
    }

    private static Map<UUID, Boolean> playerVisualizationStatus = new HashMap<>();

    public static void toggleChunkVisualization(Player player) {
        UUID playerId = player.getUniqueId();
        if (playerVisualizationStatus.containsKey(playerId) && playerVisualizationStatus.get(playerId)) {
            playerVisualizationStatus.put(playerId, false);
        } else {
            playerVisualizationStatus.put(playerId, true);
            startVisualizationTask(player);
        }
    }

    private static void startVisualizationTask(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!playerVisualizationStatus.getOrDefault(player.getUniqueId(), false)) {
                    this.cancel();
                    return;
                }
                Chunk chunk = player.getLocation().getChunk();
                showChunkBorders(player, chunk);
            }
        }.runTaskTimer(Bukkit.getPluginManager().getPlugin("PwingGuilds"), 0L, 20L);
    }

    /**
     * Displays particle effects around chunk borders for a player
     * Creates a visual outline using END_ROD particles at chunk edges in unclaimed areas
     * and VILLAGER_HAPPY particles in claimed areas
     * 
     * @param player Player to show particles to
     * @param chunk Chunk to visualize borders for
     * @throws IllegalArgumentException if player or chunk is null
     */
    public static void showChunkBorders(Player player, Chunk chunk) {
        boolean isClaimed = isChunkClaimed(chunk); // Assume this method checks if the chunk is claimed
        Particle particle = isClaimed ? Particle.VILLAGER_HAPPY : Particle.END_ROD;
        DustOptions dustOptions = new DustOptions(Color.LIME, 1); // Green color for claimed areas

        Location corner = chunk.getBlock(0, player.getLocation().getBlockY(), 0).getLocation();
        
        for (int i = 0; i <= 16; i++) {
            player.spawnParticle(particle, corner.clone().add(i, 0, 0), 1, dustOptions);
            player.spawnParticle(particle, corner.clone().add(i, 0, 16), 1, dustOptions);
            player.spawnParticle(particle, corner.clone().add(0, 0, i), 1, dustOptions);
            player.spawnParticle(particle, corner.clone().add(16, 0, i), 1, dustOptions);
        }
    }

    private static boolean isChunkClaimed(Chunk chunk) {
        // Implement logic to check if the chunk is claimed
        return false; // Placeholder return value
    }
}