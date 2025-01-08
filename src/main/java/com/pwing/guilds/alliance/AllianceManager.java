package com.pwing.guilds.alliance;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.alliance.storage.AllianceStorage;
import com.pwing.guilds.events.GuildJoinAllianceEvent;
import com.pwing.guilds.events.GuildLeaveAllianceEvent;
import org.bukkit.Bukkit;
import java.util.*;

public class AllianceManager {
    private final PwingGuilds plugin;
    private final Map<String, Alliance> alliances = new HashMap<>();
    private final Map<String, Alliance> guildAllianceMap = new HashMap<>();
    private final AllianceStorage storage;

    public AllianceManager(PwingGuilds plugin, AllianceStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void initialize() {
        Set<Alliance> loadedAlliances = storage.loadAllAlliances();
        loadedAlliances.forEach(alliance -> {
            alliances.put(alliance.getName(), alliance);
            alliance.getMembers().forEach(guild ->
                    guildAllianceMap.put(guild.getName(), alliance));
        });
        plugin.getLogger().info("Loaded " + alliances.size() + " alliances");
    }

    public boolean createAlliance(String name, Guild founder) {
        if (alliances.containsKey(name)) {
            return false;
        }

        Alliance alliance = new Alliance(name);
        alliance.addMember(founder);
        alliance.setRole(founder.getOwner(), AllianceRole.LEADER);

        alliances.put(name, alliance);
        guildAllianceMap.put(founder.getName(), alliance);
        storage.saveAlliance(alliance);
        return true;
    }

    public Optional<Alliance> getAlliance(String name) {
        return Optional.ofNullable(alliances.get(name));
    }

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

    public void updateGuildAlliance(String guildName, Alliance alliance) {
        guildAllianceMap.put(guildName, alliance);
    }

    public void removeGuildAlliance(String guildName) {
        guildAllianceMap.remove(guildName);
    }

    public Collection<Alliance> getAlliances() {
        return Collections.unmodifiableCollection(alliances.values());
    }

    public Optional<Alliance> getGuildAlliance(String guildName) {
        return Optional.ofNullable(guildAllianceMap.get(guildName));
    }

    public void saveAlliance(Alliance alliance) {
        storage.saveAlliance(alliance);
        alliances.put(alliance.getName(), alliance);
    }
}
