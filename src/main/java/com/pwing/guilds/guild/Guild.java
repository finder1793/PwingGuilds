package com.pwing.guilds.guild;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.perks.GuildPerks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Chunk;

import java.util.*;
import java.util.stream.Collectors;

public class Guild {
    private final PwingGuilds plugin;
    private final String name;
    private UUID owner;
    private UUID leader;
    private final Set<UUID> members;
    private final Set<UUID> invites;
    private final Set<ChunkLocation> claimedChunks;
    private GuildPerks perks;
    private int level;
    private long exp;
    private int bonusClaims = 0;

    public Guild(PwingGuilds plugin, String name, UUID owner) {
        this.plugin = plugin;
        this.name = name;
        this.owner = owner;
        this.members = new HashSet<>();
        this.invites = new HashSet<>();
        this.claimedChunks = new HashSet<>();
        this.members.add(owner);
        this.level = 1;
        this.exp = 0;
        this.perks = new GuildPerks(plugin, level);
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

    public boolean addInvite(UUID player) {
        return invites.add(player);
    }

    public boolean removeInvite(UUID player) {
        return invites.remove(player);
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

    public boolean removeMember(UUID player) {
        if (player.equals(owner)) return false;
        return members.remove(player);
    }

    public boolean unclaimChunk(ChunkLocation chunk) {
        return claimedChunks.remove(chunk);
    }

    public String getName() {
        return name;
    }

    public UUID getLeader() {
        return leader;
    }

    public Set<UUID> getMembers() {
        return Collections.unmodifiableSet(members);
    }

    public Set<ChunkLocation> getClaimedChunks() {
        return Collections.unmodifiableSet(claimedChunks);
    }

    public int getLevel() {
        return level;
    }

    public long getExp() {
        return exp;
    }

    public GuildPerks getPerks() {
        return perks;
    }

    public boolean addExp(long amount) {
        exp += amount * perks.getExpMultiplier();
        int nextLevel = level + 1;
        long requiredExp = plugin.getConfig().getLong("guild-levels." + nextLevel + ".exp-required");

        if (exp >= requiredExp) {
            level = nextLevel;
            perks = new GuildPerks(plugin, level);
            return true;
        }
        return false;
    }

    public void addBonusClaims(int amount) {
        this.bonusClaims += amount;
    }

    public int getBonusClaims() {
        return bonusClaims;
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

    public boolean addMember(UUID player) {
        return members.add(player);
    }

    public boolean claimChunk(ChunkLocation chunk) {
        if (canClaim()) {
            return claimedChunks.add(chunk);
        }
        return false;
    }


    public boolean promotePlayer(UUID player) {
        if (!members.contains(player)) {
            return false;
        }

        // Set the player as an officer or promote to leader depending on current role
        if (player.equals(leader)) {
            return false; // Already highest rank
        }

        leader = player;
        return true;
    }

    public UUID getOwner() {
        return owner;
    }


    public void setLevel(int level) {
        this.level = level;
    }

    public void setExp(long exp) {
        this.exp = exp;
    }


    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("owner", owner.toString());
        data.put("leader", leader.toString());
        data.put("level", level);
        data.put("exp", exp);
        data.put("members", members.stream().map(UUID::toString).collect(Collectors.toList()));
        data.put("claimed-chunks", claimedChunks.stream()
                .map(chunk -> chunk.getWorld() + "," + chunk.getX() + "," + chunk.getZ())
                .collect(Collectors.toList()));
        return data;
    }

    public static Guild deserialize(PwingGuilds plugin, Map<String, Object> data) {
        String name = (String) data.get("name");
        UUID owner = UUID.fromString((String) data.get("owner"));
        Guild guild = new Guild(plugin, name, owner);

        guild.setLevel((Integer) data.get("level"));
        guild.setExp((Long) data.get("exp"));

        ((List<String>) data.get("members")).stream()
                .map(UUID::fromString)
                .forEach(guild::addMember);

        ((List<String>) data.get("claimed-chunks")).stream()
                .map(str -> {
                    String[] parts = str.split(",");
                    return new ChunkLocation(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                })
                .forEach(guild::claimChunk);

        return guild;
    }
}



