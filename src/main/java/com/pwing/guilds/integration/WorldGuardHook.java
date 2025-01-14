package com.pwing.guilds.integration;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.ChunkLocation;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.managers.RegionManager;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * Handles integration with WorldGuard for guild claims.
 */
public class WorldGuardHook {
    private final PwingGuilds plugin;
    private final WorldGuardPlugin worldGuard;

    /**
     * Constructs a new WorldGuardHook instance.
     * @param plugin The main plugin instance
     */
    public WorldGuardHook(PwingGuilds plugin) {
        this.plugin = plugin;
        this.worldGuard = (WorldGuardPlugin) Bukkit.getPluginManager().getPlugin("WorldGuard");
    }

    /**
     * Checks if a guild can claim a chunk based on WorldGuard regions.
     * @param player The player attempting to claim the chunk
     * @param chunkLocation The chunk location to claim
     * @return true if the chunk can be claimed, false otherwise
     */
    public boolean canClaimChunk(Player player, ChunkLocation chunkLocation) {
        if (worldGuard == null) {
            plugin.getLogger().warning("WorldGuard integration unavailable.");
            return false;
        }

        World world = Bukkit.getWorld(chunkLocation.getWorld().getName());
        if (world == null) {
            plugin.getLogger().warning("WorldGuard integration unavailable or world has no players: " + chunkLocation.getWorld().getName());
            return false;
        }

        RegionManager regionManager = WorldGuard.getInstance().getPlatform().getRegionContainer().get(BukkitAdapter.adapt(world));
        if (regionManager == null) {
            plugin.getLogger().warning("WorldGuard integration unavailable or world has no players: " + chunkLocation.getWorld().getName());
            return false;
        }

        // Add your logic to check if the chunk can be claimed based on WorldGuard regions
        // For example, check if the region is owned by another player or guild

        return true;
    }
}
