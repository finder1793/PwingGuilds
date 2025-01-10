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
 * Base class for guild events that can be participated in
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

    public Map<Guild, Integer> getScores() {
        return Collections.unmodifiableMap(scores);
    }

    public void addScore(Guild guild, int points) {
        scores.merge(guild, points, Integer::sum);
    }

    public boolean isActive() {
        return isActive;
    }

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



