package com.pwing.guilds.visualization;

import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;

/**
 * Provides visual effects for displaying chunk boundaries to players.
 * Uses particles to outline chunk borders in-game.
 */
public class ChunkVisualizer {

    /**
     * Displays particle effects around chunk borders for a player
     * Creates a visual outline using END_ROD particles at chunk edges
     * 
     * @param player Player to show particles to
     * @param chunk Chunk to visualize borders for
     * @throws IllegalArgumentException if player or chunk is null
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