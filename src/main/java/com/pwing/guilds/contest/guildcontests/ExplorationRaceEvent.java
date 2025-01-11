package com.pwing.guilds.contest.guildcontests;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.Location;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;
import java.util.*;

/**
 * Event where guilds compete to explore the most new chunks
 * Awards points based on unique chunks discovered by guild members
 * Includes special checkpoints that award bonus points when found
 */
public class ExplorationRaceEvent extends GuildEvent implements Listener {
    private final Map<Guild, Set<ChunkLocation>> exploredChunks = new HashMap<>();
    private final Map<String, Location> checkpoints = new HashMap<>();
    private final Map<Guild, Set<String>> discoveredCheckpoints = new HashMap<>();
    private BukkitTask particleTask;
    
    private static final int CHECKPOINT_COUNT = 10;
    private static final double CHECKPOINT_RADIUS = 5.0;
    private static final int CHUNK_POINTS = 1;
    private static final int CHECKPOINT_POINTS = 50;

    /**
     * Creates a new exploration race event
     * @param plugin Plugin instance
     * @param name Event name
     * @param duration Duration in minutes
     */
    public ExplorationRaceEvent(PwingGuilds plugin, String name, int duration) {
        super(plugin, name, duration);
        this.description = "Explore new territories and discover hidden checkpoints!";
    }

    @Override
    public void start() {
        isActive = true;
        generateCheckpoints();
        startParticleEffects();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.broadcastMessage("§3§lExploration Race has begun!");
        Bukkit.broadcastMessage("§bFind §e" + CHECKPOINT_COUNT + "§b hidden checkpoints marked by §5particles§b!");
    }

    private void generateCheckpoints() {
        World world = Bukkit.getWorlds().get(0);
        double borderSize = world.getWorldBorder().getSize() / 2;
        Random random = new Random();

        for (int i = 0; i < CHECKPOINT_COUNT; i++) {
            Location loc;
            String checkpointId;
            do {
                int x = random.nextInt((int)borderSize * 2) - (int)borderSize;
                int z = random.nextInt((int)borderSize * 2) - (int)borderSize;
                loc = world.getHighestBlockAt(x, z).getLocation().add(0, 1, 0);
                checkpointId = loc.getWorld().getName() + ":" + loc.getBlockX() + ":" + loc.getBlockZ();
            } while (checkpoints.containsKey(checkpointId));
            
            checkpoints.put(checkpointId, loc);
        }
    }

    private void startParticleEffects() {
        particleTask = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            checkpoints.values().forEach(loc -> {
                if (loc.getWorld().isChunkLoaded(loc.getBlockX() >> 4, loc.getBlockZ() >> 4)) {
                    loc.getWorld().spawnParticle(Particle.DRAGON_BREATH, loc, 20, 0.5, 1.5, 0.5, 0);
                    loc.getWorld().spawnParticle(Particle.END_ROD, loc.add(0, 3, 0), 1, 0, 0, 0, 0);
                }
            });
        }, 20L, 20L); // Every second
    }

    /**
     * Handles player movement to track chunk exploration
     * Awards points when players discover new chunks or checkpoints
     * Updates visual and sound effects for discoveries
     * @param event The player move event
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!isActive) return;
        
        Player player = event.getPlayer();
        Optional<Guild> guildOpt = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (!guildOpt.isPresent()) return;
        
        Guild guild = guildOpt.get();
        Location playerLoc = player.getLocation();
        ChunkLocation newChunk = new ChunkLocation(playerLoc.getChunk());
        
        // Track new chunks
        if (exploredChunks.computeIfAbsent(guild, k -> new HashSet<>()).add(newChunk)) {
            player.spigot().sendMessage(net.md_5.bungee.api.ChatMessageType.ACTION_BAR, 
                net.md_5.bungee.api.chat.TextComponent.fromLegacyText(
                    "§bNew chunk discovered! (§e" + exploredChunks.get(guild).size() + "§b)"
                ));
        }
        
        // Check for nearby checkpoints
        checkpoints.forEach((id, checkpoint) -> {
            if (!discoveredCheckpoints.computeIfAbsent(guild, k -> new HashSet<>()).contains(id) 
                && playerLoc.distance(checkpoint) <= CHECKPOINT_RADIUS) {
                
                discoveredCheckpoints.get(guild).add(id);
                
                // Visual and audio feedback
                player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1.0f, 1.0f);
                player.spawnParticle(Particle.TOTEM, player.getLocation(), 100, 0.5, 0.5, 0.5, 0.5);
                
                int found = discoveredCheckpoints.get(guild).size();
                Bukkit.broadcastMessage("§3" + guild.getName() + 
                    " §bfound checkpoint §e#" + found + "§b! (§e" + found + "§b/§e" + CHECKPOINT_COUNT + "§b)");
            }
        });
    }

    @Override
    public void updateScores() {
        exploredChunks.forEach((guild, chunks) -> {
            int checkpointsFound = discoveredCheckpoints.getOrDefault(guild, new HashSet<>()).size();
            int score = (chunks.size() * CHUNK_POINTS) + (checkpointsFound * CHECKPOINT_POINTS);
            scores.put(guild, score);
        });
    }

    @Override
    public void end() {
        if (particleTask != null) {
            particleTask.cancel();
        }
        isActive = false;
        HandlerList.unregisterAll(this);
        updateScores();
        announceResults();
    }

    @Override
    public void announceResults() {
        Bukkit.broadcastMessage("§3§lExploration Race Results:");
        scores.entrySet().stream()
            .sorted(Map.Entry.<Guild, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                int checkpoints = discoveredCheckpoints.getOrDefault(entry.getKey(), new HashSet<>()).size();
                int chunks = exploredChunks.get(entry.getKey()).size();
                Bukkit.broadcastMessage("§e" + entry.getKey().getName() + 
                    " §7- §b" + checkpoints + " checkpoints, " +
                    chunks + " chunks explored");
            });
    }
}
