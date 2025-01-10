package com.pwing.guilds.events.custom;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.pwing.guilds.events.custom.EventAnnouncer;
/**
 * Handles scheduling and automated execution of guild events.
 * Events can be configured to run at specific times on specific days.
 * Provides announcement functionality for upcoming events.
 */
public class EventScheduler {
    private final PwingGuilds plugin;
    private final Map<LocalTime, ScheduledEvent> scheduledEvents = new HashMap<>();
    private final EventAnnouncer announcer;
    private final DateTimeFormatter timeFormat = DateTimeFormatter.ofPattern("HH:mm");

    public EventScheduler(PwingGuilds plugin) {
        this.plugin = plugin;
        this.announcer = new EventAnnouncer(plugin);
        loadSchedule();
        startScheduler();
    }

    /**
     * Loads event schedules from configuration
     * Parses time formats and validates event settings
     */
    private void loadSchedule() {
        ConfigurationSection scheduleSection = plugin.getConfig().getConfigurationSection("event-schedule");
        if (scheduleSection != null) {
            for (String eventName : scheduleSection.getKeys(false)) {
                if (!plugin.getConfig().getBoolean("events." + eventName + ".enabled", false)) {
                    continue;
                }

                String timeString = scheduleSection.getString(eventName + ".time");
                List<Integer> announceTimings = scheduleSection.getIntegerList(eventName + ".announce-before");
                List<String> dayStrings = scheduleSection.getStringList(eventName + ".days");
                List<DayOfWeek> days = dayStrings.stream()
                        .map(DayOfWeek::valueOf)
                        .collect(Collectors.toList());
                int minPlayers = scheduleSection.getInt(eventName + ".min-players");

                LocalTime time = LocalTime.parse(timeString, timeFormat);
                scheduledEvents.put(time, new ScheduledEvent(eventName, announceTimings, days, minPlayers));
            }
        }
    }

    /**
     * Starts the scheduler task that checks for and executes events
     * Runs every minute to check scheduled times
     */
    private void startScheduler() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            LocalTime now = LocalTime.now();
            scheduledEvents.forEach((time, event) -> {
                long minutesUntil = ChronoUnit.MINUTES.between(now, time);
                if (event.shouldAnnounce(minutesUntil)) {
                    announcer.announceUpcoming(event.getName(), (int) minutesUntil);
                }
                if (minutesUntil == 0 && event.canStart()) {
                    plugin.getEventManager().startEvent(event.getName());
                }
            });
        }, 20L, 1200L);
    }

    /**
     * Inner class representing a scheduled event with its timing configuration
     */
    private static class ScheduledEvent {
        private final String name;
        private final List<Integer> announceTimings;
        private final List<DayOfWeek> days;
        private final int minPlayers;

        public ScheduledEvent(String name, List<Integer> announceTimings, List<DayOfWeek> days, int minPlayers) {
            this.name = name;
            this.announceTimings = announceTimings;
            this.days = days;
            this.minPlayers = minPlayers;
        }

        /**
         * Checks if an announcement should be made at the given time
         * @param minutesUntil Minutes until event starts
         * @return true if announcement timing matches configuration
         */
        public boolean shouldAnnounce(long minutesUntil) {
            return announceTimings.contains((int) minutesUntil) &&
                    days.contains(LocalDate.now().getDayOfWeek());
        }

        /**
         * Checks if the event can start based on day and player count
         * @return true if conditions are met to start event
         */
        public boolean canStart() {
            return days.contains(LocalDate.now().getDayOfWeek()) &&
                    Bukkit.getOnlinePlayers().size() >= minPlayers;
        }

        public String getName() {
            return name;
        }
    }
}
