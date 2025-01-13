package com.pwing.guilds.contest.guildcontests;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.api.ChunkClaimEvent;
import com.pwing.guilds.contest.guildcontests.GuildEvent;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;

import com.pwing.guilds.guild.Guild;

import java.util.HashMap;
import java.util.Map;

/**
 * Event for controlling territory in guild contests.
 */
public class TerritoryControlEvent extends GuildEvent implements Listener {
    private Map<Guild, Integer> initialClaims = new HashMap<>();

    /**
     * Constructs a new TerritoryControlEvent.
     * 
     * @param plugin The PwingGuilds plugin instance.
     * @param name The name of the event.
     * @param duration The duration of the event.
     */
    public TerritoryControlEvent(PwingGuilds plugin, String name, int duration) {
        super(plugin, name, duration);
        this.description = "Claim the most new territory during the event!";
    }

    @Override
    public void start() {
        isActive = true;
        // Record initial claims
        plugin.getGuildManager().getGuilds().forEach(guild ->
            initialClaims.put(guild, guild.getClaimedChunks().size()));
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    @Override
    public void end() {
        isActive = false;
        HandlerList.unregisterAll(this);
        updateScores();
    }

    @Override
    public void updateScores() {
        plugin.getGuildManager().getGuilds().forEach(guild -> {
            int newClaims = guild.getClaimedChunks().size() - initialClaims.getOrDefault(guild, 0);
            addScore(guild, newClaims);
        });
    }

    /**
     * Handles chunk claim events.
     * 
     * @param event The chunk claim event.
     */
    @EventHandler
    public void onChunkClaim(ChunkClaimEvent event) {
        if (isActive) {
            updateScores();
        }
    }
}
