package com.pwing.guilds.alliance;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.alliance.storage.AllianceStorage;
import com.pwing.guilds.events.GuildJoinAllianceEvent;
import com.pwing.guilds.events.GuildLeaveAllianceEvent;
import org.bukkit.Bukkit;
import java.util.*;

/**
 * Manages all alliances in the plugin, including creation, deletion, and storage
 */
public class AllianceManager {
    private final PwingGuilds plugin;
    private final Map<String, Alliance> alliances = new HashMap<>();
    private final Map<String, Alliance> guildAllianceMap = new HashMap<>();
    private final AllianceStorage storage;

    /**
     * Creates a new AllianceManager instance
     * @param plugin The main plugin instance
     * @param storage The storage implementation to use
     */
    public AllianceManager(PwingGuilds plugin, AllianceStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    /**
     * Initializes the alliance manager and loads all alliances
     */
    public void initialize() {
        Set<Alliance> loadedAlliances = storage.loadAllAlliances();
        loadedAlliances.forEach(alliance -> {
            alliances.put(alliance.getName(), alliance);
            alliance.getMembers().forEach(guild ->
                    guildAllianceMap.put(guild.getName(), alliance));
        });
        plugin.getLogger().info("Loaded " + alliances.size() + " alliances");
    }

    /**
     * Creates a new alliance
     * @param name The name of the alliance
     * @param owner The founding guild
     * @return The created Alliance instance
     */
    public Alliance createAlliance(String name, Guild owner) {
        Alliance alliance = new Alliance(name);
        alliance.addMember(owner);
        alliances.put(name, alliance);
        return alliance;
    }

    /**
     * Gets an alliance by name
     * @param name The name of the alliance
     * @return Optional containing the alliance if found
     */
    public Optional<Alliance> getAlliance(String name) {
        return Optional.ofNullable(alliances.get(name));
    }

    /**
     * Deletes an alliance by name
     * @param name The name of the alliance to delete
     */
    public void deleteAlliance(String name) {
        Alliance alliance = alliances.remove(name);
        if (alliance != null) {
            alliance.getMembers().forEach(guild -> {
                guild.setAlliance(null);
                guildAllianceMap.remove(guild.getName());
            });
            storage.deleteAlliance(name);
        }
    }

    /**
     * Adds a guild to an alliance
     * @param allianceName The name of the alliance
     * @param guild The guild to add
     * @return true if the guild was added, false otherwise
     */
    public boolean addGuildToAlliance(String allianceName, Guild guild) {
        Alliance alliance = alliances.get(allianceName);
        if (alliance != null) {
            GuildJoinAllianceEvent event = new GuildJoinAllianceEvent(guild, alliance);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                alliance.addMember(guild);
                guildAllianceMap.put(guild.getName(), alliance);
                storage.saveAlliance(alliance);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes a guild from an alliance
     * @param allianceName The name of the alliance
     * @param guild The guild to remove
     * @return true if the guild was removed, false otherwise
     */
    public boolean removeGuildFromAlliance(String allianceName, Guild guild) {
        Alliance alliance = alliances.get(allianceName);
        if (alliance != null) {
            GuildLeaveAllianceEvent event = new GuildLeaveAllianceEvent(guild, alliance);
            Bukkit.getPluginManager().callEvent(event);

            if (!event.isCancelled()) {
                alliance.removeMember(guild);
                guildAllianceMap.remove(guild.getName());
                storage.saveAlliance(alliance);
                return true;
            }
        }
        return false;
    }

    /**
     * Updates the alliance of a guild
     * @param guildName The name of the guild
     * @param alliance The new alliance
     */
    public void updateGuildAlliance(String guildName, Alliance alliance) {
        guildAllianceMap.put(guildName, alliance);
    }

    /**
     * Removes the alliance of a guild
     * @param guildName The name of the guild
     */
    public void removeGuildAlliance(String guildName) {
        guildAllianceMap.remove(guildName);
    }

    /**
     * Gets all registered alliances
     * @return Collection of all alliances
     */
    public Collection<Alliance> getAllAlliances() {
        return Collections.unmodifiableCollection(alliances.values());
    }

    /**
     * Gets the alliance of a guild
     * @param guildName The name of the guild
     * @return Optional containing the alliance if found
     */
    public Optional<Alliance> getGuildAlliance(String guildName) {
        return Optional.ofNullable(guildAllianceMap.get(guildName));
    }

    /**
     * Saves an alliance to storage
     * @param alliance The alliance to save
     */
    public void saveAlliance(Alliance alliance) {
        storage.saveAlliance(alliance);
        alliances.put(alliance.getName(), alliance);
    }

    /**
     * Adds an alliance to the manager
     * @param alliance The alliance to add
     */
    public void addAlliance(Alliance alliance) {
        alliances.put(alliance.getName(), alliance);
    }
}
