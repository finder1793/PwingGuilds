package com.pwing.guilds.events.custom;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import java.time.Duration;
import java.util.*;

/**
 * Base class for all guild events.
 * Provides core functionality for event timing, scoring and rewards.
 */
public abstract class GuildEvent implements Listener {
    /** The plugin instance */
    protected final PwingGuilds plugin;
    
    /** The name of this event */
    protected final String name;
    
    /** The description of this event */
    protected String description;
    
    /** How long the event lasts */
    protected Duration duration;
    
    /** Tracks scores for participating guilds */
    protected Map<Guild, Integer> scores = new HashMap<>();
    
    /** Whether the event is currently running */
    protected boolean isActive = false;

    private final Map<Guild, Map<String, Integer>> eventStats = new HashMap<>();
    protected final List<String> eventMessages = new ArrayList<>();

    /**
     * Creates a new guild event
     * @param plugin The plugin instance
     * @param name The name of the event
     * @param durationMinutes How long the event should last in minutes
     */
    public GuildEvent(PwingGuilds plugin, String name, int durationMinutes) {
        this.plugin = plugin;
        this.name = name;
        this.duration = Duration.ofMinutes(durationMinutes);
    }

    /**
     * Starts the guild event
     */
    public abstract void start();

    /**
     * Updates the scores for the event
     */
    public abstract void updateScores();

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Duration getDuration() {
        return duration;
    }

    /**
     * Gets the current scores for all guilds
     * @return Unmodifiable map of guilds to their scores
     */
    public Map<Guild, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    /**
     * Adds score for a guild in this event
     * @param guild The guild to add points to
     * @param points Number of points to add
     */
    public void addScore(Guild guild, int points) {
        scores.merge(guild, points, Integer::sum);
    }

    public boolean isActive() {
        return isActive;
    }

    /**
     * Distributes rewards to participating guilds based on placement
     * Rewards are configured in the plugin config file
     */  
    public void distributeRewards() {
        plugin.getRewardManager().giveEventRewards(this);
    }

    /**
     * Announces the results of the event
     */
    public void announceResults() {
        Bukkit.broadcastMessage("§6§l" + name + " Results:");
        
        // Announce top 3 scores
        scores.entrySet().stream()
            .sorted(Map.Entry.<Guild, Integer>comparingByValue().reversed())
            .limit(3)
            .forEach(entry -> {
                Guild guild = entry.getKey();
                int score = entry.getValue();
                Bukkit.broadcastMessage("§e" + guild.getName() + " §7- §6" + score + " points");
                
                // Show guild's stats if any
                Map<String, Integer> stats = getGuildStats(guild);
                if (!stats.isEmpty()) {
                    stats.forEach((stat, value) -> 
                        Bukkit.broadcastMessage("  §7" + stat + ": §f" + value));
                }
            });

        // Show event messages
        if (!eventMessages.isEmpty()) {
            Bukkit.broadcastMessage("§6§lHighlights:");
            eventMessages.forEach(msg -> Bukkit.broadcastMessage("§7- " + msg));
        }
    }

    /**
     * Records a statistic for a guild during the event
     * @param guild The guild to record for
     * @param stat The name of the statistic
     * @param value The value to add
     */
    protected void recordStat(Guild guild, String stat, int value) {
        eventStats.computeIfAbsent(guild, k -> new HashMap<>())
                 .merge(stat, value, Integer::sum);
    }

    /**
     * Gets all recorded stats for a guild
     * @param guild The guild to get stats for
     * @return Map of stat names to values
     */
    public Map<String, Integer> getGuildStats(Guild guild) {
        return Collections.unmodifiableMap(
            eventStats.getOrDefault(guild, Collections.emptyMap())
        );
    }

    /**
     * Adds a message to be displayed in the event summary
     * @param message The message to add
     */
    protected void addEventMessage(String message) {
        eventMessages.add(message);
    }

    /**
     * Ends the guild event
     */
    public void end() {
        isActive = false;
        HandlerList.unregisterAll(this);
        updateScores();
        distributeRewards();
        announceResults();
    }
}



