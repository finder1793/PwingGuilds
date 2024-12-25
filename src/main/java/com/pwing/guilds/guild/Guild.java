package com.pwing.guilds.guild;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.perks.GuildPerks;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.Chunk;
import java.util.*;

public class Guild {
    private final PwingGuilds plugin;
    private final String name;
    private UUID owner;
    private final Set<UUID> members;
    private final Set<UUID> invites;
    private final Set<ChunkLocation> claimedChunks;
    private GuildPerks perks;
    private int level;
    private long exp;

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

    public String getName() { return name; }
    public UUID getOwner() { return owner; }
    public Set<UUID> getMembers() { return Collections.unmodifiableSet(members); }
    public Set<ChunkLocation> getClaimedChunks() { return Collections.unmodifiableSet(claimedChunks); }
    public int getLevel() { return level; }
    public long getExp() { return exp; }
    public GuildPerks getPerks() { return perks; }

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

    public boolean canClaim() {
        int maxClaims = plugin.getConfig().getInt("guild-levels." + level + ".max-claims");
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

}

