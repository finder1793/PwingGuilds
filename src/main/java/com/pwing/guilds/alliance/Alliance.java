package com.pwing.guilds.alliance;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.*;
import java.util.stream.Collectors;

public class Alliance implements ConfigurationSerializable {
    private final String name;
    private final Set<Guild> members = new HashSet<>();
    private final String ownerGuild;
    private Set<Guild> pendingInvites = new HashSet<>();
    private Set<String> allies = new HashSet<>();
    private final Map<UUID, AllianceRole> roles = new HashMap<>();
    private final Set<String> tags = new HashSet<>();
    private String description;

    public Alliance(String name) {
        this.name = name;
        this.ownerGuild = name;
    }

    public boolean addMember(Guild guild) {
        if (members.add(guild)) {
            guild.setAlliance(this);
            return true;
        }
        return false;
    }

    public boolean inviteGuild(Guild guild) {
        if (members.contains(guild)) {
            return false;
        }
        return pendingInvites.add(guild);
    }

    public boolean acceptInvite(Guild guild) {
        if (pendingInvites.remove(guild)) {
            return members.add(guild);
        }
        return false;
    }

    public boolean declineInvite(Guild guild) {
        return pendingInvites.remove(guild);
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

    public boolean addAlly(String allyName) {
        return allies.add(allyName);
    }

    public boolean removeAlly(String allyName) {
        return allies.remove(allyName);
    }

    public boolean isAlly(String allyName) {
        return allies.contains(allyName);
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
        data.put("allies", new ArrayList<>(allies));
        return data;
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

    public Set<String> getAllies() {
        return Collections.unmodifiableSet(allies);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getOwnerGuild() {
        return ownerGuild;
    }

    public Set<Guild> getPendingInvites() {
        return Collections.unmodifiableSet(pendingInvites);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, members, allies);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Alliance other = (Alliance) obj;
        return Objects.equals(name, other.name) 
               && Objects.equals(members, other.members)
               && Objects.equals(allies, other.allies);
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

    public boolean removeMember(Guild guild) {
        if (members.remove(guild)) {
            guild.setAlliance(null);
            return true;
        }
        return false;
    }
}

