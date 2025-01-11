package com.pwing.guilds.guild;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.perks.GuildPerks;
import com.pwing.guilds.storage.GuildStorage;
import com.pwing.guilds.alliance.AllianceManager;
import com.pwing.guilds.events.*;
import com.pwing.guilds.alliance.Alliance;

import org.bukkit.inventory.ItemStack;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;

import java.util.*;
import java.util.stream.Collectors;

@SerializableAs("Guild")
/**
 * Represents a guild in the plugin.
 * A guild is a player-created organization that can claim territory, manage members,
 * and provide benefits to its members through various perks and upgrades.
 */
public class Guild implements ConfigurationSerializable {
    private final PwingGuilds plugin;
    private final String name;
    private final UUID owner;
    private UUID leader;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> invites = new HashSet<>();
    private final Set<ChunkLocation> claimedChunks = new HashSet<>();
    private final Map<String, GuildHome> homes = new HashMap<>();
    private GuildPerks perks;
    private int level;
    private long exp;
    private int bonusClaims;
    private Alliance alliance;
    private long lastUpdate;
    private final Set<UUID> onlineMembers = new HashSet<>();
    private boolean pvpEnabled = false;  // Default PvP off in guild territories

    /**
     * Creates a new guild with the specified parameters
     * @param plugin The plugin instance
     * @param name The name of the guild
     * @param owner The UUID of the guild owner
     */
    public Guild(PwingGuilds plugin, String name, UUID owner) {
        this.plugin = plugin;
        this.name = name;
        this.owner = owner;
        this.members.add(owner);
        this.level = 1;
        this.exp = 0;
        this.perks = new GuildPerks(plugin, this, level);
    }

    /**
     * Invites a player to join the guild
     * @param player The UUID of the player to invite
     * @return true if invite was successful, false if already invited
     */
    public boolean invite(UUID player) {
        return invites.add(player);
    }

    /**
     * Accepts a player's invitation to join the guild
     * @param player The UUID of the player
     * @return true if successful, false if no invite exists
     */
    public boolean acceptInvite(UUID player) {
        if (invites.remove(player)) {
            return members.add(player);
        }
        return false;
    }

    /**
     * Checks if a player has a pending invite to this guild
     * @param player The player's UUID to check
     * @return true if the player has an invite, false otherwise
     */
    public boolean hasInvite(UUID player) {
        return invites.contains(player);
    }

    /**
     * Checks if a player is a member of this guild
     * @param player Player UUID to check
     * @return true if player is a member
     */
    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    /**
     * Checks if a given chunk is claimed by this guild
     * @param chunk The chunk to check
     * @return true if the chunk is claimed by this guild
     */
    public boolean isChunkClaimed(Chunk chunk) {
        return claimedChunks.contains(new ChunkLocation(chunk));
    }

    /**
     * Claims a chunk of land for the guild
     * @param chunk The chunk location to claim
     * @return true if claim was successful, false if already claimed or at claim limit
     */
    public boolean claimChunk(ChunkLocation chunk) {
        if (canClaim()) {
            boolean claimed = claimedChunks.add(chunk);
            // Don't save during deserialization
            if (claimed && plugin.getGuildManager() != null) {
                plugin.getGuildManager().getStorage().saveGuild(this);
            }
            return claimed;
        }
        return false;
    }

    /**
     * Removes a guild's claim on a chunk of land
     * @param chunk The chunk location to unclaim
     * @return true if unclaim was successful, false if not claimed
     */
    public boolean unclaimChunk(ChunkLocation chunk) {
        boolean unclaimed = claimedChunks.remove(chunk);
        if (unclaimed) {
            plugin.getGuildManager().getStorage().saveGuild(this);
        }
        return unclaimed;
    }
    /**
     * Adds experience points to the guild
     * @param amount The amount of exp to add
     * @return true if level up occurred
     */
    public boolean addExp(long amount) {
        GuildExpGainEvent expEvent = new GuildExpGainEvent(this, amount);
        Bukkit.getPluginManager().callEvent(expEvent);

        if (expEvent.isCancelled()) {
            return false;
        }

        long oldExp = exp;
        int oldLevel = level;

        exp += expEvent.getAmount() * perks.getExpMultiplier();

        int nextLevel = level + 1;
        long requiredExp = plugin.getConfig().getLong("guild-levels." + nextLevel + ".exp-required");

        if (exp >= requiredExp) {
            GuildLevelUpEvent levelEvent = new GuildLevelUpEvent(this, oldLevel, nextLevel);
            Bukkit.getPluginManager().callEvent(levelEvent);

            if (!levelEvent.isCancelled()) {
                level = nextLevel;
                perks = new GuildPerks(plugin, this, level);
                return true;
            } else {
                exp = oldExp;
            }
        }

        return false;
    }

    /**
     * Adds bonus claim chunks to the guild
     * @param amount The amount of bonus claims to add
     */
    public void addBonusClaims(int amount) {
        this.bonusClaims += amount;
    }

    /**
     * Checks if the guild can claim more chunks based on level and bonus claims
     * @return true if guild can claim more chunks, false if at limit
     */
    public boolean canClaim() {
        int maxClaims = plugin.getConfig().getInt("guild-levels." + level + ".max-claims") + bonusClaims;
        return claimedChunks.size() < maxClaims;
    }

    /**
     * Checks if the guild can add another member
     * @return true if guild has space for another member, false otherwise
     */
    public boolean canAddMember() {
        return members.size() < perks.getMemberLimit();
    }

    /**
     * Broadcasts a message to all online guild members
     * @param message The message to broadcast
     */
    public void broadcastMessage(String message) {
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }


    /**
     * Promotes a member to guild leader
     * @param player UUID of the player to promote
     * @return true if promotion was successful
     */
    public boolean promotePlayer(UUID player) {
        if (!members.contains(player) || player.equals(leader)) {
            return false;
        }
        leader = player;
        return true;
    }

    /**
     * Creates or updates a guild home location
     * @param name The name of the home
     * @param location The location of the home
     * @return true if home was set successfully
     */
    public boolean setHome(String name, Location location) {
        GuildHomeCreateEvent event = new GuildHomeCreateEvent(this, name, location);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            homes.put(name, new GuildHome(name, location));
            return true;
        }
        return false;
    }

    /**
     * Gets a guild home by name
     * @param name The name of the home
     * @return Optional containing the home if it exists
     */
    public Optional<GuildHome> getHome(String name) {
        return Optional.ofNullable(homes.get(name.toLowerCase()));
    }

    /**
     * Removes a guild home
     * @param name The name of the home to delete
     * @return true if home was deleted successfully
     */
    public boolean deleteHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }
    /**
     * Creates a new guild member
     * @param player UUID of the player to add
     * @return true if member was added successfully, false otherwise
     */
    public boolean addMember(UUID player) {
        GuildMemberJoinEvent event = new GuildMemberJoinEvent(this, player);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            boolean added = members.add(player);
            if (added) {
                plugin.getGuildManager().getStorage().saveGuild(this);
            }
            return added;
        }
        return false;
    }

    /**
     * Removes a member from the guild
     * @param player UUID of player to remove
     * @param reason Reason for removal
     * @return true if player was removed successfully
     */
    public boolean removeMember(UUID player, GuildMemberLeaveEvent.LeaveReason reason) {
        if (members.remove(player)) {
            Bukkit.getPluginManager().callEvent(new GuildMemberLeaveEvent(this, player, reason));
            plugin.getGuildManager().getStorage().saveGuild(this);
            return true;
        }
        return false;
    }
    

    /**
     * Sets the guild's name
     * @param newName New name for guild
     * @return true if name was changed successfully
     */
    public boolean setName(String newName) {
        GuildRenameEvent event = new GuildRenameEvent(this, this.name, newName);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            plugin.getGuildManager().updateGuildName(this, event.getNewName());
            return true;
        }
        return false;
    }
    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("owner", owner.toString());
        data.put("level", level);
        data.put("exp", exp);
        data.put("bonus-claims", bonusClaims);
        data.put("members", members.stream().map(UUID::toString).collect(Collectors.toList()));
        data.put("invites", invites.stream().map(UUID::toString).collect(Collectors.toList()));
        data.put("claimed-chunks", claimedChunks.stream()
                .sorted((c1, c2) -> {
                    int worldCompare = c1.getWorldName().compareTo(c2.getWorldName());
                    if (worldCompare != 0) return worldCompare;
                    int xCompare = Integer.compare(c1.getX(), c2.getX());
                    if (xCompare != 0) return xCompare;
                    return Integer.compare(c1.getZ(), c2.getZ());
                })
                .map(chunk -> Map.of(
                        "world", chunk.getWorld(),
                        "x", chunk.getX(),
                        "z", chunk.getZ()
                )).collect(Collectors.toList()));
        data.put("homes", homes.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        e -> serializeLocation(e.getValue().getLocation())
                )));
        if (alliance != null) {
            data.put("alliance", alliance.getName());
        }
        data.put("pvp-enabled", pvpEnabled);
        return data;
    }

    private Map<String, Object> serializeLocation(Location loc) {
        return Map.of(
                "world", loc.getWorld().getName(),
                "x", loc.getX(),
                "y", loc.getY(),
                "z", loc.getZ(),
                "yaw", loc.getYaw(),
                "pitch", loc.getPitch()
        );
    }

    /**
     * Deserializes guild data from configuration
     * @param plugin Plugin instance
     * @param data Map of serialized data
     * @return The deserialized guild
     */
    public static Guild deserialize(PwingGuilds plugin, Map<String, Object> data) {
        String name = (String) data.get("name");
        UUID owner = UUID.fromString((String) data.get("owner"));
        Guild guild = new Guild(plugin, name, owner);

        guild.setLevel((Integer) data.get("level"));
        
        // Fix the exp casting issue
        Object expObj = data.get("exp");
        if (expObj instanceof Integer) {
            guild.setExp(((Integer) expObj).longValue());
        } else if (expObj instanceof Long) {
            guild.setExp((Long) expObj);
        }

        // Handle possible Integer for bonus claims
        Object bonusClaimsObj = data.getOrDefault("bonus-claims", 0);
        if (bonusClaimsObj instanceof Integer) {
            guild.addBonusClaims((Integer) bonusClaimsObj);
        }

        @SuppressWarnings("unchecked")
        List<String> membersList = (List<String>) data.get("members");
        membersList.stream()
                .map(UUID::fromString)
                .forEach(guild::addMember);

        @SuppressWarnings("unchecked")
        List<String> invitesList = (List<String>) data.get("invites");
        if (invitesList != null) {
            invitesList.stream()
                    .map(UUID::fromString)
                    .forEach(guild.invites::add);
        }

        // Fix the claims loading
        @SuppressWarnings("unchecked")
        List<String> claimsList = (List<String>) data.get("claims");
        if (claimsList != null) {
            claimsList.forEach(claimString -> {
                String[] parts = claimString.split(",");
                if (parts.length >= 3) {
                    ChunkLocation claim = new ChunkLocation(
                        parts[0], // world
                        Integer.parseInt(parts[1]), // x
                        Integer.parseInt(parts[2])  // z
                    );
                    guild.claimChunk(claim);
                }
            });
        }

        @SuppressWarnings("unchecked")
        Map<String, Map<String, Object>> homes = (Map<String, Map<String, Object>>) data.get("homes");
        if (homes != null) {
            homes.forEach((homeName, locationData) -> {
                Location loc = new Location(
                        Bukkit.getWorld((String) locationData.get("world")),
                        (Double) locationData.get("x"),
                        (Double) locationData.get("y"),
                        (Double) locationData.get("z"),
                        ((Number) locationData.get("yaw")).floatValue(),
                        ((Number) locationData.get("pitch")).floatValue()
                );
                guild.setHome(homeName, loc);
            });
        }

        if (data.containsKey("alliance")) {
            String allianceName = (String) data.get("alliance");
            plugin.getAllianceManager().getAlliance(allianceName)
                    .ifPresent(guild::setAlliance);
        }

        guild.setPvPEnabled((Boolean) data.getOrDefault("pvp-enabled", false));

        return guild;
    }

    // Getters
    /** @return The guild's name */
    public String getName() { return name; }
    /** @return The guild owner's UUID */
    public UUID getOwner() { return owner; }
    /** @return The guild's current experience points */
    public long getExp() { return exp; }
    /** @return The guild's current level */
    public int getLevel() { return level; }
    /** @return UUID of the guild leader */
    public UUID getLeader() { return leader; }
    /**
     * Gets an unmodifiable set of all guild members
     * @return Set of member UUIDs
     */
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
    /**
     * Gets an unmodifiable set of all claimed chunks
     * @return Set of claimed chunk locations
     */
    public Set<ChunkLocation> getClaimedChunks() { return Collections.unmodifiableSet(claimedChunks); }
    /**
     * Gets the guild's perks manager
     * @return GuildPerks instance
     */
    public GuildPerks getPerks() { return perks; }
    /**
     * Gets the number of bonus claim chunks
     * @return Number of bonus claims
     */
    public int getBonusClaims() { return bonusClaims; }
    /**
     * Gets an unmodifiable map of all guild homes
     * @return Map of home names to GuildHome objects
     */
    public Map<String, GuildHome> getHomes() { return Collections.unmodifiableMap(homes); }
    /**
     * Gets the guild's current alliance
     * @return Alliance object or null if not in an alliance
     */
    public Alliance getAlliance() { return alliance; }
    /**
     * Checks if PvP is enabled in guild claimed territories
     * @return true if PvP is enabled, false if disabled
     */
    public boolean isPvPEnabled() {
        return pvpEnabled;
    }

    /**
     * Sets whether PvP is enabled in guild territories
     * @param enabled true to enable PvP, false to disable
     */
    public void setPvPEnabled(boolean enabled) {
        this.pvpEnabled = enabled;
        plugin.getGuildManager().getStorage().saveGuild(this);
    }

    /**
     * Checks if PvP is allowed between two players in guild territory
     * @param player1 First player
     * @param player2 Second player
     * @return true if PvP is allowed between these players
     */
    public boolean isPvPAllowed(Player player1, Player player2) {
        if (!pvpEnabled) {
            return false;
        }
        
        // If both players are guild members, check if friendly fire is enabled
        boolean areBothMembers = isMember(player1.getUniqueId()) && isMember(player2.getUniqueId());
        if (areBothMembers) {
            return plugin.getConfig().getBoolean("guild-settings.allow-friendly-fire", false);
        }
        
        return true;
    }

    // Setters
    /**
     * Sets the guild's level
     * @param level New level value
     */
    public void setLevel(int level) {
        this.level = level;
        this.perks = new GuildPerks(plugin, this, level);
    }
    /**
     * Sets the guild's experience points
     * @param exp New experience amount
     */
    public void setExp(long exp) { this.exp = exp; }
    /**
     * Sets the guild's current alliance
     * @param alliance Alliance to set
     */
    public void setAlliance(Alliance alliance) { this.alliance = alliance; }


    /**
     * Gets all items stored in guild storage
     * @return Array of stored items
     */
    public ItemStack[] getStorageContents() {
        return plugin.getStorageManager().getGuildStorage(this.name);
    }

    /**
     * Gets all online guild members
     * @return List of online players in guild
     */
    public List<Player> getOnlineMembers() {
        return members.stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .collect(Collectors.toList());
    }

    /**
     * Updates the member list and validates all members
     */
    public void updateMemberList() {
        // Update last activity timestamp
        this.lastUpdate = System.currentTimeMillis();
        
        // Update online status for members
        for (UUID memberId : members) {
            Player player = Bukkit.getPlayer(memberId);
            if (player != null && player.isOnline()) {
                onlineMembers.add(memberId);
            } else {
                onlineMembers.remove(memberId);
            }
        }
        
        // Save changes
        plugin.getGuildManager().getStorage().saveGuild(this);
    }

    /**
     * Checks if the guild bank has enough money
     * @param amount Amount to check for
     * @return true if the guild has enough money
     */
    public boolean hasMoney(double amount) {
        UUID leaderId = getLeader();
        if (leaderId == null) return false;
        
        return plugin.getEconomy() != null && 
               plugin.getEconomy().has(Bukkit.getOfflinePlayer(leaderId), amount);
    }

    /**
     * Withdraws money from the guild bank
     * @param amount Amount to withdraw
     * @return true if withdrawal was successful
     */
    public boolean withdrawMoney(double amount) {
        UUID leaderId = getLeader();
        if (leaderId == null || !hasMoney(amount)) return false;

        plugin.getEconomy().withdrawPlayer(
            Bukkit.getOfflinePlayer(leaderId), 
            amount
        );
        return true;
    }
}