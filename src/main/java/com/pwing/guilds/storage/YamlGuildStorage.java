package com.pwing.guilds.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;

import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class YamlGuildStorage implements GuildStorage {
    private final PwingGuilds plugin;
    private final File guildsFolder;

    public YamlGuildStorage(PwingGuilds plugin) {
        this.plugin = plugin;
        this.guildsFolder = new File(plugin.getDataFolder(), "guilds");
        if (!guildsFolder.exists()) {
            guildsFolder.mkdirs();
        }
    }

    @Override
    public void saveGuild(Guild guild) {
        File guildFile = new File(guildsFolder, guild.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();
        
        config.set("name", guild.getName());
        config.set("owner", guild.getOwner().toString());
        config.set("level", guild.getLevel());
        config.set("exp", guild.getExp());
        
        config.set("members", guild.getMembers().stream()
                .map(UUID::toString)
                .toList());
        
        config.set("claimed-chunks", guild.getClaimedChunks().stream()
                .map(chunk -> chunk.getWorld() + "," + chunk.getX() + "," + chunk.getZ())
                .toList());

        try {
            config.save(guildFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Guild loadGuild(String name) {
        File guildFile = new File(guildsFolder, name + ".yml");
        if (!guildFile.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(guildFile);
        UUID owner = UUID.fromString(config.getString("owner"));
        Guild guild = new Guild(plugin, name, owner);

        // Load members
        config.getStringList("members").stream()
                .map(UUID::fromString)
                .forEach(uuid -> guild.addMember(uuid));

        // Load claimed chunks
        config.getStringList("claimed-chunks").stream()
                .map(str -> {
                    String[] parts = str.split(",");
                    ChunkLocation chunk = new ChunkLocation(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                    return chunk;
                })
                .forEach(chunk -> guild.claimChunk((ChunkLocation) chunk));

        return guild;
    }

    @Override
    public Set<Guild> loadAllGuilds() {
        Set<Guild> guilds = new HashSet<>();
        File[] files = guildsFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        
        if (files != null) {
            for (File file : files) {
                String guildName = file.getName().replace(".yml", "");
                Guild guild = loadGuild(guildName);
                if (guild != null) {
                    guilds.add(guild);
                }
            }
        }
        
        return guilds;
    }

    @Override
    public void deleteGuild(String name) {
        File guildFile = new File(guildsFolder, name + ".yml");
        if (guildFile.exists()) {
            guildFile.delete();
        }
    }
}
