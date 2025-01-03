package com.pwing.guilds.events.custom;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.events.custom.GuildEvent;
import com.pwing.guilds.events.custom.GuildEventManager;
import com.pwing.guilds.events.custom.TerritoryControlEvent;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

public class GuildEventManager {
    private final PwingGuilds plugin;
    private final Map<String, GuildEvent> activeEvents = new HashMap<>();
    private final Map<String, GuildEvent> scheduledEvents = new HashMap<>();
    private final FileConfiguration eventConfig;

    public GuildEventManager(PwingGuilds plugin) {
        this.plugin = plugin;
        this.eventConfig = YamlConfiguration.loadConfiguration(
            new File(plugin.getDataFolder(), "events.yml"));
        loadEvents();
        startEventScheduler();
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

    public void startEvent(String eventName) {
        GuildEvent event = scheduledEvents.get(eventName);
        if (event != null && !activeEvents.containsKey(eventName)) {
            event.start();
            activeEvents.put(eventName, event);
            Bukkit.broadcastMessage("§6§lGuild Event: §e" + eventName + " §ahas started!");

            // Schedule event end
            Bukkit.getScheduler().runTaskLater(plugin, () -> endEvent(eventName),
                event.getDuration().toMinutes() * 1200L);
        }
    }

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

    public Map<String, GuildEvent> getActiveEvents() {
        return Collections.unmodifiableMap(activeEvents);
    }
}
