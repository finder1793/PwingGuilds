package com.pwing.guilds.alliance;

import com.pwing.guilds.guild.Guild;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.*;

public class Alliance implements ConfigurationSerializable {
    private final String name;
    private final Set<Guild> members;
    private final Map<UUID, AllianceRole> roles;
    private final AlliancePerks perks;
    private final Set<String> tags;
    private String description;

    public Alliance(String name) {
        this.name = name;
        this.members = new HashSet<>();
        this.roles = new HashMap<>();
        this.perks = new AlliancePerks();
        this.tags = new HashSet<>();
    }

    public boolean addMember(Guild guild) {
        return members.add(guild);
    }

    public boolean removeMember(Guild guild) {
        return members.remove(guild);
    }

    public void setRole(UUID player, AllianceRole role) {
        roles.put(player, role);
    }

    public AllianceRole getRole(UUID player) {
        return roles.getOrDefault(player, AllianceRole.MEMBER);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("members", members.stream().map(Guild::getName).toList());
        data.put("roles", roles);
        data.put("perks", perks);
        data.put("tags", tags);
        data.put("description", description);
        return data;
    }

    public Set<Guild> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public String getName() {
        return name;
    }
}
