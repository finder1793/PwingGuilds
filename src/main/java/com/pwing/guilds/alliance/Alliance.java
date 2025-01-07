package com.pwing.guilds.alliance;

import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.*;

public class Alliance implements ConfigurationSerializable {
    private final String name;
    private final Set<Guild> members;
    private final Map<UUID, AllianceRole> roles;
    private final Set<String> tags;
    private String description;

    public Alliance(String name) {
        this.name = name;
        this.members = new HashSet<>();
        this.roles = new HashMap<>();
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Map<UUID, AllianceRole> getRoles() {
        return Collections.unmodifiableMap(roles);
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public static Alliance deserialize(Map<String, Object> data, PwingGuilds plugin) {
        String name = (String) data.get("name");
        Alliance alliance = new Alliance(name);

        List<String> memberNames = (List<String>) data.get("members");
        memberNames.stream()
                .map(plugin.getGuildManager()::getGuild)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .forEach(alliance::addMember);

        Map<String, String> rolesMap = (Map<String, String>) data.get("roles");
        rolesMap.forEach((uuid, role) ->
                alliance.setRole(UUID.fromString(uuid), AllianceRole.valueOf(role)));

        alliance.tags.addAll((List<String>) data.get("tags"));
        alliance.description = (String) data.get("description");

        return alliance;
    }
}
