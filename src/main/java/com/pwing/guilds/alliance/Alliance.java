package com.pwing.guilds.alliance;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Represents an alliance between multiple guilds that can share benefits and work together.
 */
public class Alliance implements ConfigurationSerializable {
    private final String name;
    private final Set<Guild> members = new HashSet<>();
    private final String ownerGuild;
    private Set<Guild> pendingInvites = new HashSet<>();
    private Set<String> allies = new HashSet<>();
    private final Map<UUID, AllianceRole> roles = new HashMap<>();
    private final Set<String> tags = new HashSet<>();
    private String description;

    /**
     * Creates a new alliance with the given name
     * @param name The name of the alliance
     */
    public Alliance(String name) {
        this.name = name;
        this.ownerGuild = name;
    }

    /**
     * Adds a member guild to this alliance
     * @param guild The guild to add
     * @return true if the guild was added successfully
     */
    public boolean addMember(Guild guild) {
        if (members.add(guild)) {
            guild.setAlliance(this);
            return true;
        }
        return false;
    }

    /**
     * Invites a guild to join this alliance
     * @param guild The guild to invite
     * @return true if the invite was sent successfully
     */
    public boolean inviteGuild(Guild guild) {
        if (members.contains(guild)) {
            return false;
        }
        return pendingInvites.add(guild);
    }

    /**
     * Accepts an alliance invitation for the given guild
     * @param guild The guild accepting the invite
     * @return true if the invite was accepted successfully
     */
    public boolean acceptInvite(Guild guild) {
        if (pendingInvites.remove(guild)) {
            return members.add(guild);
        }
        return false;
    }

    /**
     * Declines a pending alliance invitation
     * @param guild The guild declining the invite
     * @return true if invite was successfully declined
     */
    public boolean declineInvite(Guild guild) {
        return pendingInvites.remove(guild);
    }

    /**
     * Updates a player's role in the alliance
     * @param player The player's UUID
     * @param role The new role to assign
     */
    public void setRole(UUID player, AllianceRole role) {
        roles.put(player, role);
    }

    /**
     * Remove a player's role in the alliance
     * @param player UUID of the player
     */
    public void removeRole(UUID player) {
        roles.remove(player);
    }

    /**
     * Get a player's alliance role
     * @param player UUID of the player
     * @return The player's alliance role
     */
    public AllianceRole getRole(UUID player) {
        return roles.getOrDefault(player, AllianceRole.MEMBER);
    }

    /**
     * Check if a player has a role in the alliance
     * @param player UUID of the player to check
     * @return True if the player has a role, false otherwise
     */
    public boolean hasRole(UUID player) {
        return roles.containsKey(player);
    }

    /**
     * Add a tag to this alliance
     * @param tag The tag to add
     */
    public void addTag(String tag) {
        tags.add(tag);
    }

    /**
     * Remove a tag from the alliance
     * @param tag Tag to remove
     */
    public void removeTag(String tag) {
        tags.remove(tag);
    }

    /**
     * Check if the alliance has a specific tag
     * @param tag Tag to check
     * @return True if the alliance has the tag, false otherwise
     */
    public boolean hasTag(String tag) {
        return tags.contains(tag);
    }

    /**
     * Add an ally to this alliance
     * @param allyName The name of the ally to add
     * @return True if successfully added, false otherwise
     */
    public boolean addAlly(String allyName) {
        return allies.add(allyName);
    }

    /**
     * Remove an ally from the alliance
     * @param allyName Name of the ally to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeAlly(String allyName) {
        return allies.remove(allyName);
    }

    /**
     * Check if a guild is an ally
     * @param allyName Name of the guild to check
     * @return True if the guild is an ally, false otherwise
     */
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
    /**
     * Get the alliance name
     * @return The name of the alliance
     */
    public String getName() {
        return name;
    }

    /**
     * Get all member guilds in this alliance
     * @return Set of member guilds
     */
    public Set<Guild> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    /**
     * Get all roles in the alliance
     * @return Map of player UUIDs to their roles
     */
    public Map<UUID, AllianceRole> getRoles() {
        return Collections.unmodifiableMap(roles);
    }

    /**
     * Get all tags associated with this alliance
     * @return Set of alliance tags
     */
    public Set<String> getTags() {
        return Collections.unmodifiableSet(tags);
    }

    /**
     * Get all allies of this alliance
     * @return Set of ally names
     */
    public Set<String> getAllies() {
        return Collections.unmodifiableSet(allies);
    }

    /**
     * Get the alliance description
     * @return The description of the alliance
     */
    public String getDescription() {
        return description;
    }

    /**
     * Set the alliance description
     * @param description New description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Get the owner guild's name
     * @return Name of the owner guild
     */
    public String getOwnerGuild() {
        return ownerGuild;
    }

    /**
     * Get all pending guild invites
     * @return Set of guilds with pending invites
     */
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

    /**
     * Deserializes an alliance from configuration data
     * @param data The serialized alliance data
     * @param plugin The plugin instance
     * @return The deserialized Alliance
     */
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

    /**
     * Remove a member guild from the alliance
     * @param guild Guild to remove
     * @return True if successfully removed, false otherwise
     */
    public boolean removeMember(Guild guild) {
        if (members.remove(guild)) {
            guild.setAlliance(null);
            return true;
        }
        return false;
    }
}

