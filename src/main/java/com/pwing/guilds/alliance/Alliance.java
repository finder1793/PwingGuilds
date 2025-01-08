package com.pwing.guilds.alliance;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.*;
import java.util.stream.Collectors;

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
        if (members.add(guild)) {
            guild.setAlliance(this);
            return true;
        }
        return false;
    }

    public boolean removeMember(Guild guild) {
        if (members.remove(guild)) {
            guild.setAlliance(null);
            return true;
        }
        return false;
    }

    public void setRole(UUID player, AllianceRole role) {
        roles.put(player, role);
    }

    public void removeRole(UUID player) {
        roles.remove(player);
    }

    public AllianceRole getRole(UUID player) {
        return roles.getOrDefault(player, AllianceRole.MEMBER);
    }

    public boolean hasRole(UUID player) {
        return roles.containsKey(player);
    }

    public void addTag(String tag) {
        tags.add(tag);
    }

    public void removeTag(String tag) {
        tags.remove(tag);
    }

    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("members", members.stream()
                .map(Guild::getName)
                .collect(Collectors.toList()));
        data.put("roles", roles.entrySet().stream()
                .collect(Collectors.toMap(
                        entry -> entry.getKey().toString(),
                        entry -> entry.getValue().name()
                )));
        data.put("tags", new ArrayList<>(tags));
        data.put("description", description);
        return data;
    }

    @SuppressWarnings("unchecked")
    public static Alliance deserialize(Map<String, Object> data, PwingGuilds plugin) {
        String name = (String) data.get("name");
        Alliance alliance = new Alliance(name);

        List<String> memberNames = (List<String>) data.get("members");
        if (memberNames != null) {
            memberNames.stream()
                    .map(plugin.getGuildManager()::getGuild)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(alliance::addMember);
        }

        Map<String, String> rolesMap = (Map<String, String>) data.get("roles");
        if (rolesMap != null) {
            rolesMap.forEach((uuid, role) ->
                    alliance.setRole(UUID.fromString(uuid), AllianceRole.valueOf(role)));
        }

        List<String> tagsList = (List<String>) data.get("tags");
        if (tagsList != null) {
            alliance.tags.addAll(tagsList);
        }

        alliance.description = (String) data.get("description");

        return alliance;
    }

    // Getters
    public String getName() {
        return name;
    }

    public Set<Guild> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public Map<UUID, AllianceRole> getRoles() {
        return Collections.unmodifiableMap(roles);
    }

    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Alliance)) return false;
        Alliance other = (Alliance) o;
        return name.equals(other.name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
