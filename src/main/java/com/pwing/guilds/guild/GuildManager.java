package com.pwing.guilds.guild;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import java.util.*;
import java.util.Collection;
import com.pwing.guilds.events.GuildCreateEvent;
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

    public boolean createGuild(String name, UUID owner) {
        if (guilds.containsKey(name)) {
            return false;
        }
        Guild guild = new Guild(plugin, name, owner);
        guilds.put(name, guild);
        Bukkit.getPluginManager().callEvent(new GuildCreateEvent(guild));
        return true;
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
        claimedChunks.put(location, guild);
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
            return guild.invite(invited);
        }
        return false;
    }

    public boolean acceptInvite(String guildName, UUID player) {
        Guild guild = guilds.get(guildName);
        if (guild != null && guild.acceptInvite(player)) {
            playerGuilds.put(player, guild);
            return true;
        }
        return false;
    }

    public boolean kickMember(String guildName, UUID kicker, UUID kicked) {
        Guild guild = guilds.get(guildName);
        if (guild != null && guild.getLeader().equals(kicker)) {
            if (guild.removeMember(kicked)) {
                playerGuilds.remove(kicked);
                return true;
            }
        }
        return false;
    }

    public boolean unclaimChunk(Guild guild, Chunk chunk) {
        ChunkLocation location = new ChunkLocation(chunk);
        if (claimedChunks.get(location) == guild) {
            claimedChunks.remove(location);
            return guild.unclaimChunk(location);
        }
        return false;
    }

    public Collection<Guild> getGuilds() {
        return guilds.values();
    }


    public void addGuild(Guild guild) {
        guilds.put(guild.getName(), guild);
    }
}
