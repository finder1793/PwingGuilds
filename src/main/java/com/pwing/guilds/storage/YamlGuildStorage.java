package com.pwing.guilds.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;
import com.pwing.guilds.guild.GuildHome;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class YamlGuildStorage implements GuildStorage {
    private final PwingGuilds plugin;
    private final File guildsFolder;
    private final Map<String, Guild> guildCache = new HashMap<>();
    private static final long AUTO_SAVE_INTERVAL = 6000L;

    public YamlGuildStorage(PwingGuilds plugin) {
        this.plugin = plugin;
        this.guildsFolder = new File(plugin.getDataFolder(), "guilds");
        if (!guildsFolder.exists()) {
            guildsFolder.mkdirs();
        }
        startAutoSave();
    }

    private void startAutoSave() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getLogger().info("Starting auto-save of all guilds...");
            for (Guild guild : guildCache.values()) {
                saveGuild(guild);
            }
            plugin.getLogger().info("Auto-save complete!");
        }, AUTO_SAVE_INTERVAL, AUTO_SAVE_INTERVAL);
    }
    @Override
    public void saveGuild(Guild guild) {
        File guildFile = new File(guildsFolder, guild.getName() + ".yml");
        YamlConfiguration config = new YamlConfiguration();

        // Core guild data
        config.set("name", guild.getName());
        config.set("owner", guild.getOwner().toString());
        config.set("level", guild.getLevel());
        config.set("exp", guild.getExp());
        config.set("bonus-claims", guild.getBonusClaims());

        // Members
        config.set("members", guild.getMembers().stream()
                .map(UUID::toString)
                .toList());

        // Claimed chunks
        config.set("claimed-chunks", guild.getClaimedChunks().stream()
            .map(chunk -> Map.of(
                "world", chunk.getWorld(),
                "x", chunk.getX(),
                "z", chunk.getZ()
            )).collect(Collectors.toList()));

        // Guild homes
        config.set("homes", guild.getHomes().entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> serializeLocation(e.getValue().getLocation())
                )));

        try {
            config.save(guildFile);
            guildCache.put(guild.getName(), guild);
            createBackup(guild.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save guild: " + guild.getName());
            e.printStackTrace();
        }
    }
    private void createBackup(String guildName) {
        File guildFile = new File(guildsFolder, guildName + ".yml");
        File backupFolder = new File(guildsFolder, "backups");
        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }

        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File backupFile = new File(backupFolder, guildName + "-" + timestamp + ".yml");

        try {
            Files.copy(guildFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create backup for guild: " + guildName);
        }
    }
    @Override
    public Guild loadGuild(String name) {
        if (guildCache.containsKey(name)) {
            return guildCache.get(name);
        }

        File guildFile = new File(guildsFolder, name + ".yml");
        if (!guildFile.exists()) return null;

        YamlConfiguration config = YamlConfiguration.loadConfiguration(guildFile);
        UUID owner = UUID.fromString(config.getString("owner"));
        Guild guild = new Guild(plugin, name, owner);

        guild.setLevel(config.getInt("level"));
        guild.setExp(config.getLong("exp"));
        guild.addBonusClaims(config.getInt("bonus-claims", 0));

        // Load members with explicit handling
        List<String> membersList = config.getStringList("members");
        for (String memberUUID : membersList) {
            UUID memberId = UUID.fromString(memberUUID);
            guild.addMember(memberId);
            plugin.getGuildManager().getPlayerGuilds().put(memberId, guild);
        }

        // Load claimed chunks
        config.getStringList("claimed-chunks").stream()
            .map(str -> {
                String[] parts = str.split(",");
                return new ChunkLocation(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            })
            .forEach(guild::claimChunk);

        guildCache.put(name, guild);
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
        guildCache.remove(name);
        File guildFile = new File(guildsFolder, name + ".yml");
        if (guildFile.exists()) {
            createBackup(name); // Create one final backup before deletion
            guildFile.delete();
        }
    }
    public void cleanupOldBackups(int daysToKeep) {
        File backupFolder = new File(guildsFolder, "backups");
        if (!backupFolder.exists()) return;

        long cutoffTime = System.currentTimeMillis() - (daysToKeep * 24 * 60 * 60 * 1000L);
        File[] backups = backupFolder.listFiles();

        if (backups != null) {
            for (File backup : backups) {
                if (backup.lastModified() < cutoffTime) {
                    backup.delete();
                }
            }
        }
    }

    private String serializeLocation(Location location) {
        return location.getWorld().getName() + "," +
            location.getX() + "," +
           location.getY() + "," +
           location.getZ() + "," +
           location.getYaw() + "," +
           location.getPitch();
    }
}