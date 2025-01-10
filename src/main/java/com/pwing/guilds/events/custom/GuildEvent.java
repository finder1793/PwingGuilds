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
    protected final PwingGuilds plugin;
    protected final String name;
    protected String description;
    protected Duration duration;
    protected Map<Guild, Integer> scores = new HashMap<>();
    protected boolean isActive = false;

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



