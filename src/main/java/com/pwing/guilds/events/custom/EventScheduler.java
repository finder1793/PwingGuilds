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

    private void loadSchedule() {
        ConfigurationSection scheduleSection = plugin.getConfig().getConfigurationSection("event-schedule");
        if (scheduleSection != null) {
            for (String eventName : scheduleSection.getKeys(false)) {
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

        public boolean shouldAnnounce(long minutesUntil) {
            return announceTimings.contains((int) minutesUntil) &&
                    days.contains(LocalDate.now().getDayOfWeek());
        }

        public boolean canStart() {
            return days.contains(LocalDate.now().getDayOfWeek()) &&
                    Bukkit.getOnlinePlayers().size() >= minPlayers;
        }

        public String getName() {
            return name;
        }
    }
}
