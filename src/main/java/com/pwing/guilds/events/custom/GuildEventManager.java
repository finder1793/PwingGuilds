package com.pwing.guilds.events.custom;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.events.custom.GuildEvent;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import com.pwing.guilds.config.ConfigValidator;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages custom guild events.
 * Handles starting, stopping and tracking guild events.
 */
public class GuildEventManager {
    private final PwingGuilds plugin;
    private final Map<String, GuildEvent> activeEvents = new HashMap<>();
    private final Map<String, GuildEvent> scheduledEvents = new HashMap<>();
    private final FileConfiguration eventConfig;

    /**
     * Creates a new guild event manager
     * @param plugin The plugin instance
     */
    public GuildEventManager(PwingGuilds plugin) {
        this.plugin = plugin;
        this.eventConfig = YamlConfiguration.loadConfiguration(
            new File(plugin.getDataFolder(), "events.yml"));
        validateConfig();
        loadEvents();
        startEventScheduler();
    }

    private void validateConfig() {
        if (!eventConfig.contains("events")) {
            plugin.getLogger().warning("Missing events section!");
            plugin.getLogger().severe("Configuration validation failed! Please fix the errors above.");
        }
    }

    private void loadEvents() {
        if (eventConfig.contains("scheduled-events")) {
            for (String eventName : eventConfig.getConfigurationSection("scheduled-events").getKeys(false)) {
                String type = eventConfig.getString("scheduled-events." + eventName + ".type");
                String time = eventConfig.getString("scheduled-events." + eventName + ".time");
                int duration = eventConfig.getInt("scheduled-events." + eventName + ".duration");

                GuildEvent event = createEvent(type, eventName, duration);
                if (event != null) {
                    scheduledEvents.put(eventName, event);
                }
            }
        }
    }

    private void startEventScheduler() {
        // Check scheduled events every minute
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            LocalDateTime now = LocalDateTime.now();
            scheduledEvents.forEach((name, event) -> {
                String scheduledTime = eventConfig.getString("scheduled-events." + name + ".time");
                if (shouldStartEvent(scheduledTime, now)) {
                    startEvent(name);
                }
            });
        }, 1200L, 1200L);
    }

    private boolean shouldStartEvent(String scheduledTime, LocalDateTime now) {
        try {
            LocalTime eventTime = LocalTime.parse(scheduledTime);
            return now.toLocalTime().equals(eventTime);
        } catch (DateTimeParseException e) {
            return false;
        }
    }

    /**
     * Starts a guild event
     * @param eventName Name of the event to start
     */
    public void startEvent(String eventName) {
        if (!eventConfig.getBoolean("events." + eventName + ".enabled", false)) {
            return;
        }

        GuildEvent event = switch (eventName.toLowerCase()) {
            case "territory-control" -> new TerritoryControlEvent(plugin, eventName, 60);
            case "pvp-tournament" -> new PvPTournamentEvent(plugin, eventName, 45);
            case "resource-race" -> new ResourceRaceEvent(plugin, eventName, 30);
            case "boss-raid" -> new BossRaidEvent(plugin, eventName, 90);
            default -> null;
        };

        if (event != null) {
            activeEvents.put(eventName, event);
            event.start();
            Bukkit.broadcastMessage("§6§lGuild Event: §e" + eventName + " §ahas started!");

            // Schedule event end
            Bukkit.getScheduler().runTaskLater(plugin, () -> endEvent(eventName),
                event.getDuration().toMinutes() * 1200L);
        }
    }

    /**
     * Ends a guild event
     * @param eventName Name of the event to end
     */
    public void endEvent(String eventName) {
        GuildEvent event = activeEvents.remove(eventName);
        if (event != null) {
            event.end();
            announceWinners(event);
        }
    }

    private void announceWinners(GuildEvent event) {
        List<Map.Entry<Guild, Integer>> topGuilds = event.getScores().entrySet().stream()
            .sorted(Map.Entry.<Guild, Integer>comparingByValue().reversed())
            .limit(3)
            .collect(Collectors.toList());

        Bukkit.broadcastMessage("§6§lEvent Results: §e" + event.getName());
        for (int i = 0; i < topGuilds.size(); i++) {
            Map.Entry<Guild, Integer> entry = topGuilds.get(i);
            Bukkit.broadcastMessage("§e" + (i + 1) + ". §f" + entry.getKey().getName() +
                " §7- §6" + entry.getValue() + " points");
        }
    }

    private GuildEvent createEvent(String type, String name, int duration) {
        return switch (type.toLowerCase()) {
            case "territory" -> new TerritoryControlEvent(plugin, name, duration);
            case "resource" -> new ResourceRaceEvent(plugin, name, duration);
            case "pvp" -> new PvPTournamentEvent(plugin, name, duration);
            case "raid" -> new BossRaidEvent(plugin, name, duration);
            default -> null;
        };
    }

    /**
     * Gets all currently active events
     * @return Map of event names to event instances
     */
    public Map<String, GuildEvent> getActiveEvents() {
        return Collections.unmodifiableMap(activeEvents);
    }
}
