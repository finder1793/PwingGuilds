package com.pwing.guilds.alliance;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.alliance.storage.AllianceStorage;
import org.bukkit.Bukkit;
import java.util.*;

public class AllianceManager {
    private final PwingGuilds plugin;
    private final Map<String, Alliance> alliances = new HashMap<>();
    private final AllianceStorage storage;

    public AllianceManager(PwingGuilds plugin, AllianceStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
    }

    public void initialize() {
        Set<Alliance> loadedAlliances = storage.loadAllAlliances();
        loadedAlliances.forEach(alliance -> alliances.put(alliance.getName(), alliance));
    }

    public boolean createAlliance(String name, Guild founder) {
        if (alliances.containsKey(name)) {
            return false;
        }

        Alliance alliance = new Alliance(name);
        alliance.addMember(founder);
        alliance.setRole(founder.getOwner(), AllianceRole.LEADER);

        alliances.put(name, alliance);
        founder.setAlliance(alliance);
        storage.saveAlliance(alliance);
        return true;
    }

    public Optional<Alliance> getAlliance(String name) {
        return Optional.ofNullable(alliances.get(name));
    }

    public void deleteAlliance(String name) {
        Alliance alliance = alliances.remove(name);
        if (alliance != null) {
            alliance.getMembers().forEach(guild -> guild.setAlliance(null));
            storage.deleteAlliance(name);
        }
    }

    public Collection<Alliance> getAlliances() {
        return Collections.unmodifiableCollection(alliances.values());
    }
}