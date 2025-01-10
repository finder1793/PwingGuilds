package com.pwing.guilds.visualization;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Utility class for visualizing chunk borders to players
 */
public class ChunkVisualizer {

    /**
     * Shows chunk borders to a player for the specified chunk
     * @param player The player to show the borders to
     * @param chunk The chunk to visualize
     */
    public static void showChunkBorders(Player player, Chunk chunk) {
        Location corner = chunk.getBlock(0, player.getLocation().getBlockY(), 0).getLocation();
        
        for (int i = 0; i <= 16; i++) {
            player.spawnParticle(Particle.END_ROD, corner.clone().add(i, 0, 0), 1);
            player.spawnParticle(Particle.END_ROD, corner.clone().add(i, 0, 16), 1);
            player.spawnParticle(Particle.END_ROD, corner.clone().add(0, 0, i), 1);
            player.spawnParticle(Particle.END_ROD, corner.clone().add(16, 0, i), 1);
        }
    }
}