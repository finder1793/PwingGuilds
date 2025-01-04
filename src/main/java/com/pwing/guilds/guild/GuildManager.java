package com.pwing.guilds.guild;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import java.util.*;
import java.util.Collection;
import com.pwing.guilds.events.GuildCreateEvent;
import com.pwing.guilds.events.GuildClaimChunkEvent;
import com.pwing.guilds.events.GuildMemberLeaveEvent;
import com.pwing.guilds.storage.GuildStorage;
import com.pwing.guilds.events.GuildDeleteEvent;



public class GuildManager {
    private final PwingGuilds plugin;
    private final Map<String, Guild> guilds;
    private final Map<UUID, Guild> playerGuilds = new HashMap<>();
    private final Map<ChunkLocation, Guild> claimedChunks = new HashMap<>();
    private final GuildStorage storage;

    public GuildManager(PwingGuilds plugin, GuildStorage storage) {
        this.plugin = plugin;
        this.storage = storage;
        this.guilds = new HashMap<>();
    }
    public void initialize() {
        Set<Guild> loadedGuilds = storage.loadAllGuilds();
        loadedGuilds.forEach(guild -> {
            guilds.put(guild.getName(), guild);
            guild.getMembers().forEach(member -> playerGuilds.put(member, guild));
        });
    }

    public boolean createGuild(String name, UUID owner) {
        if (guilds.containsKey(name)) {
            return false;
        }
        Guild guild = new Guild(plugin, name, owner);
        guilds.put(name, guild);
        playerGuilds.put(owner, guild);
        storage.saveGuild(guild);
        Bukkit.getPluginManager().callEvent(new GuildCreateEvent(guild));
        return true;
    }

    public void addGuild(Guild guild) {
        guilds.put(guild.getName(), guild);
        guild.getMembers().forEach(member -> playerGuilds.put(member, guild));
        storage.saveGuild(guild);
    }

    public void deleteGuild(String name) {
        Guild guild = guilds.get(name);
        if (guild != null) {
            Bukkit.getPluginManager().callEvent(new GuildDeleteEvent(guild));
            guilds.remove(name);
            storage.deleteGuild(name);
        }
    }
    public boolean claimChunk(Guild guild, Chunk chunk) {
        ChunkLocation location = new ChunkLocation(chunk);
        if (claimedChunks.containsKey(location)) {
            return false;
        }
        
        GuildClaimChunkEvent event = new GuildClaimChunkEvent(guild, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }
        
        claimedChunks.put(location, guild);
        storage.saveGuild(guild);
        return true;
    }
    

    public boolean canInteract(UUID player, Chunk chunk) {
        ChunkLocation location = new ChunkLocation(chunk);
        Guild guild = claimedChunks.get(location);
        return guild == null || guild.isMember(player);
    }

    public Optional<Guild> getGuildByChunk(Chunk chunk) {
        return Optional.ofNullable(claimedChunks.get(new ChunkLocation(chunk)));
    }

    public Optional<Guild> getPlayerGuild(UUID player) {
        return Optional.ofNullable(playerGuilds.get(player));
    }
    public boolean invitePlayer(String guildName, UUID inviter, UUID invited) {
        Guild guild = guilds.get(guildName);
        if (guild != null && guild.isMember(inviter)) {
            boolean success = guild.invite(invited);
            if (success) {
                storage.saveGuild(guild);
            }
            return success;
        }
        return false;
    }

    public boolean acceptInvite(String guildName, UUID player) {
        Guild guild = guilds.get(guildName);
        if (guild != null && guild.acceptInvite(player)) {
            playerGuilds.put(player, guild);
            storage.saveGuild(guild);
            return true;
        }
        return false;
    }

    public boolean kickMember(String guildName, UUID kicker, UUID kicked) {
        Guild guild = guilds.get(guildName);
        if (guild != null && guild.getLeader().equals(kicker)) {
            if (guild.removeMember(kicked, GuildMemberLeaveEvent.LeaveReason.KICKED)) {
                playerGuilds.remove(kicked);
                storage.saveGuild(guild);
                return true;
            }
        }
        return false;
    }
    public boolean unclaimChunk(Guild guild, Chunk chunk) {
        ChunkLocation location = new ChunkLocation(chunk);
        if (claimedChunks.get(location) == guild) {
            claimedChunks.remove(location);
            boolean success = guild.unclaimChunk(location);
            if (success) {
                storage.saveGuild(guild);
            }
            return success;
        }
        return false;
    }

    public Collection<Guild> getGuilds() {
        return guilds.values();
    }
    public Optional<Guild> getGuild(String name) {
        return guilds.values().stream()
                .filter(guild -> guild.getName().equalsIgnoreCase(name))
                .findFirst();
    }

    public void updateGuildName(Guild guild, String newName) {
        guilds.remove(guild.getName());
        Guild newGuild = new Guild(plugin, newName, guild.getOwner());
        // Copy over all guild data
        newGuild.setLevel(guild.getLevel());
        newGuild.setExp(guild.getExp());
        guild.getMembers().forEach(newGuild::addMember);
        guilds.put(newName, newGuild);
        storage.saveGuild(newGuild);
    }
}