package com.pwing.guilds.events.custom;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Represents a building competition event where guilds compete by placing blocks
 * Scores are based on number of blocks placed and variety of block types used
 */
public class BuildingContestEvent extends GuildEvent implements Listener {
    private final Map<Guild, Integer> blocksPlaced = new HashMap<>();
    private final Map<Guild, Integer> uniqueBlockTypes = new HashMap<>();
    private final Map<Guild, Set<Material>> usedBlockTypes = new HashMap<>();
    private final Map<Location, Material> originalBlocks = new HashMap<>();

    /**
     * Creates a new building contest event
     * @param plugin Plugin instance
     * @param name Name of the event
     * @param duration Duration in minutes
     */
    public BuildingContestEvent(PwingGuilds plugin, String name, int duration) {
        super(plugin, name, duration);
        this.description = "Build the most impressive structure in your guild's territory!";
    }

    @Override
    public void start() {
        isActive = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.broadcastMessage("§d§lBuilding Contest has begun! Show your creative skills!");
    }

    @Override
    public void updateScores() {
        blocksPlaced.forEach((guild, blocks) -> {
            int uniqueTypes = uniqueBlockTypes.getOrDefault(guild, 0);
            int score = blocks + (uniqueTypes * 10); // Bonus for variety
            scores.put(guild, score);
        });
    }

    /**
     * Handles block placement during the event
     * Awards points based on block type and tracks statistics
     * @param event Block place event
     */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!isActive) return;

        Player player = event.getPlayer();
        Optional<Guild> guildOpt = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (!guildOpt.isPresent()) return;

        Guild guild = guildOpt.get();
        Block block = event.getBlock();
        Material material = block.getType();

        // Only count blocks placed in guild territory
        if (!guild.isChunkClaimed(block.getChunk())) {
            player.sendMessage("§cBlocks must be placed in your guild's territory!");
            event.setCancelled(true);
            return;
        }

        // Track block placement
        blocksPlaced.merge(guild, 1, Integer::sum);
        
        // Track unique block types
        Set<Material> types = usedBlockTypes.computeIfAbsent(guild, k -> new HashSet<>());
        if (types.add(material)) {
            // New block type bonus
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 2.0f);
            player.sendMessage("§d+10 Building Points for using " + formatMaterialName(material) + "!");
        }

        // Store original block for cleanup
        originalBlocks.put(block.getLocation(), event.getBlockReplacedState().getType());

        // Visual feedback
        if (blocksPlaced.get(guild) % 100 == 0) {
            player.spawnParticle(Particle.VILLAGER_HAPPY, block.getLocation(), 20, 0.5, 0.5, 0.5, 0);
            Bukkit.broadcastMessage("§d" + guild.getName() + " has placed " + blocksPlaced.get(guild) + " blocks!");
        }
    }

    @Override
    public void end() {
        // Optionally restore original blocks
        if (plugin.getConfig().getBoolean("events.building-contest.restore-blocks", true)) {
            originalBlocks.forEach((loc, type) -> loc.getBlock().setType(type));
        }
        super.end();
    }

    private String formatMaterialName(Material material) {
        return material.name()
            .replace('_', ' ')
            .toLowerCase()
            .substring(0, 1)
            .toUpperCase() + 
            material.name()
            .replace('_', ' ')
            .toLowerCase()
            .substring(1);
    }
}
