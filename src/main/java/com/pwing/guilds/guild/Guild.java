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
 * Represents a guild within the plugin.
 * A guild is a group of players that can claim land, level up, and work together.
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

    public boolean hasInvite(UUID player) {
        return invites.contains(player);
    }

    public boolean isMember(UUID player) {
        return members.contains(player);
    }

    public boolean isChunkClaimed(Chunk chunk) {
        return claimedChunks.contains(new ChunkLocation(chunk));
    }

    public boolean claimChunk(ChunkLocation chunk) {
        if (canClaim()) {
            boolean claimed = claimedChunks.add(chunk);
            if (claimed) {
                plugin.getGuildManager().getStorage().saveGuild(this);
            }
            return claimed;
        }
        return false;
    }

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

    public boolean canClaim() {
        int maxClaims = plugin.getConfig().getInt("guild-levels." + level + ".max-claims") + bonusClaims;
        return claimedChunks.size() < maxClaims;
    }

    public boolean canAddMember() {
        return members.size() < perks.getMemberLimit();
    }

    public void broadcastMessage(String message) {
        for (UUID uuid : members) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null && player.isOnline()) {
                player.sendMessage(message);
            }
        }
    }


    public boolean promotePlayer(UUID player) {
        if (!members.contains(player) || player.equals(leader)) {
            return false;
        }
        leader = player;
        return true;
    }

    public boolean setHome(String name, Location location) {
        GuildHomeCreateEvent event = new GuildHomeCreateEvent(this, name, location);
        Bukkit.getPluginManager().callEvent(event);

        if (!event.isCancelled()) {
            homes.put(name, new GuildHome(name, location));
            return true;
        }
        return false;
    }

    public Optional<GuildHome> getHome(String name) {
        return Optional.ofNullable(homes.get(name.toLowerCase()));
    }

    public boolean deleteHome(String name) {
        return homes.remove(name.toLowerCase()) != null;
    }
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

    public boolean removeMember(UUID player, GuildMemberLeaveEvent.LeaveReason reason) {
        if (members.remove(player)) {
            Bukkit.getPluginManager().callEvent(new GuildMemberLeaveEvent(this, player, reason));
            plugin.getGuildManager().getStorage().saveGuild(this);
            return true;
        }
        return false;
    }
    

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
                    int worldCompare = c1.getWorld().compareTo(c2.getWorld());
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

    public static Guild deserialize(PwingGuilds plugin, Map<String, Object> data) {
        String name = (String) data.get("name");
        UUID owner = UUID.fromString((String) data.get("owner"));
        Guild guild = new Guild(plugin, name, owner);

        guild.setLevel((Integer) data.get("level"));
        guild.setExp((Long) data.get("exp"));
        guild.addBonusClaims((Integer) data.getOrDefault("bonus-claims", 0));

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

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> chunksList = (List<Map<String, Object>>) data.get("claimed-chunks");
        chunksList.stream()
                .map(chunkData -> new ChunkLocation(
                        (String) chunkData.get("world"),
                        (Integer) chunkData.get("x"),
                        (Integer) chunkData.get("z")))
                .forEach(guild::claimChunk);

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
    public UUID getLeader() { return leader; }
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
    public Set<ChunkLocation> getClaimedChunks() { return Collections.unmodifiableSet(claimedChunks); }
    public GuildPerks getPerks() { return perks; }
    public int getBonusClaims() { return bonusClaims; }
    public Map<String, GuildHome> getHomes() { return Collections.unmodifiableMap(homes); }
    public Alliance getAlliance() { return alliance; }

    // Setters
    public void setLevel(int level) {
        this.level = level;
        this.perks = new GuildPerks(plugin, this, level);
    }
    public void setExp(long exp) { this.exp = exp; }
    public void setAlliance(Alliance alliance) { this.alliance = alliance; }


    public ItemStack[] getStorageContents() {
        return plugin.getStorageManager().getGuildStorage(this.name);
    }

    public List<Player> getOnlineMembers() {
        return members.stream()
                .map(Bukkit::getPlayer)
                .filter(player -> player != null && player.isOnline())
                .collect(Collectors.toList());
    }

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