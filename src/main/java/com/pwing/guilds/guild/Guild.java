package com.pwing.guilds.guild;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.perks.GuildPerks;
import com.pwing.guilds.storage.GuildStorage;
import com.pwing.guilds.events.*;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.SerializableAs;


import java.util.*;
import java.util.stream.Collectors;

@SerializableAs("Guild")
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


    public boolean unclaimChunk(ChunkLocation chunk) {
        return claimedChunks.remove(chunk);
    }
    public boolean addExp(long amount) {
        // Fire exp gain event first
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


    public boolean claimChunk(ChunkLocation chunk) {
        if (canClaim()) {
            return claimedChunks.add(chunk);
        }
        return false;
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
            members.add(player);
            return true;
        }
        return false;
    }

    public boolean removeMember(UUID player, GuildMemberLeaveEvent.LeaveReason reason) {
        if (members.remove(player)) {
            Bukkit.getPluginManager().callEvent(new GuildMemberLeaveEvent(this, player, reason));
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
        data.put("claimed-chunks", claimedChunks.stream()
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

        return guild;
    }

    // Getters
    public String getName() { return name; }
    public UUID getOwner() { return owner; }
    public UUID getLeader() { return leader; }
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
    public Set<ChunkLocation> getClaimedChunks() { return Collections.unmodifiableSet(claimedChunks); }
    public int getLevel() { return level; }
    public long getExp() { return exp; }
    public GuildPerks getPerks() { return perks; }
    public int getBonusClaims() { return bonusClaims; }
    public Map<String, GuildHome> getHomes() { return Collections.unmodifiableMap(homes); }
    // Setters
    public void setLevel(int level) {
        this.level = level;
        this.perks = new GuildPerks(plugin, this, level);
    }
    public void setExp(long exp) { this.exp = exp; }
}