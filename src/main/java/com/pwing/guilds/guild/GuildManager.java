package com.pwing.guilds.guild;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Chunk;
import java.util.*;

public class GuildManager {
    private final PwingGuilds plugin;
    private final Map<String, Guild> guilds = new HashMap<>();
    private final Map<UUID, Guild> playerGuilds = new HashMap<>();
    private final Map<ChunkLocation, Guild> claimedChunks = new HashMap<>();

    public GuildManager(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    public boolean createGuild(String name, UUID owner) {
        if (guilds.containsKey(name)) {
            return false;
        }
        guilds.put(name, new Guild(plugin, name, owner));
        return true;
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
        if (guild != null && guild.getOwner().equals(kicker)) {
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
}