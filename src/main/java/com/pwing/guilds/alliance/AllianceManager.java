package com.pwing.guilds.alliance;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class AllianceManager {
    private final Map<String, Alliance> alliances;
    private final PwingGuilds plugin;

    public AllianceManager(PwingGuilds plugin) {
        this.plugin = plugin;
        this.alliances = new HashMap<>();
    }

    public Alliance createAlliance(String name, Guild founder) {
        if (alliances.containsKey(name)) {
            return null;
        }

        Alliance alliance = new Alliance(name);
        alliance.addMember(founder);
        alliance.setRole(founder.getOwner(), AllianceRole.LEADER);
        alliances.put(name, alliance);

        return alliance;
    }

    public void disbandAlliance(Alliance alliance) {
        alliances.remove(alliance.getName());
    }

    public Optional<Alliance> getAlliance(String name) {
        return Optional.ofNullable(alliances.get(name));
    }

    public Map<String, Alliance> getAlliances() {
        return new HashMap<>(alliances);
    }
}
