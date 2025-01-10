package com.pwing.guilds.events.custom;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.rewards.RewardManager;

import com.pwing.guilds.guild.Guild;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


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
        // Implement logic to announce the results of the event
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



