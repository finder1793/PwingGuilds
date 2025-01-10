package com.pwing.guilds.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;
import com.pwing.guilds.guild.GuildHome;
import com.pwing.guilds.guild.GuildManager;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

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
    private final GuildManager guildManager;

    public YamlGuildStorage(PwingGuilds plugin) {
        this.plugin = plugin;
        this.guildManager = plugin.getGuildManager();
        this.guildsFolder = new File(plugin.getDataFolder(), "guilds");
        if (!guildsFolder.exists()) {
            guildsFolder.mkdirs();
        }
        initBackupSettings();
        startAutoSave();
        startBackupCleaner();
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    private void startAutoSave() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getLogger().info("Starting auto-save of all guilds...");
            for (Guild guild : new ArrayList<>(guildCache.values())) {
                try {
                    saveGuild(guild);
                } catch (Exception e) {
                    plugin.getLogger().severe("Failed to auto-save guild: " + guild.getName());
                    e.printStackTrace();
                }
            }
            plugin.getLogger().info("Auto-save complete!");
        }, AUTO_SAVE_INTERVAL, AUTO_SAVE_INTERVAL);
    }

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

        // Claims
        List<String> claimStrings = guild.getClaimedChunks().stream()
                .map(claim -> claim.getWorld() + "," + claim.getX() + "," + claim.getZ())
                .collect(Collectors.toList());
        config.set("claims", claimStrings);

        // Homes
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

        // Load members
        List<String> membersList = config.getStringList("members");
        for (String memberUUID : membersList) {
            UUID memberId = UUID.fromString(memberUUID);
            guild.addMember(memberId);
            plugin.getGuildManager().getPlayerGuilds().put(memberId, guild);
        }

        // Load claims
        List<String> claimStrings = config.getStringList("claims");
        for (String claimString : claimStrings) {
            String[] parts = claimString.split(",");
            ChunkLocation claim = new ChunkLocation(parts[0], Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
            guild.claimChunk(claim);
        }

        // Load homes if they exist
        ConfigurationSection homesSection = config.getConfigurationSection("homes");
        if (homesSection != null) {
            for (String homeName : homesSection.getKeys(false)) {
                String locationString = homesSection.getString(homeName);
                if (locationString != null) {
                    String[] parts = locationString.split(",");
                    Location loc = new Location(
                            Bukkit.getWorld(parts[0]),
                            Double.parseDouble(parts[1]),
                            Double.parseDouble(parts[2]),
                            Double.parseDouble(parts[3]),
                            Float.parseFloat(parts[4]),
                            Float.parseFloat(parts[5])
                    );
                    guild.setHome(homeName, loc);
                }
            }
        }

        guildCache.put(name, guild);
        return guild;
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

    public void deleteGuild(String name) {
        guildCache.remove(name);
        File guildFile = new File(guildsFolder, name + ".yml");
        if (guildFile.exists()) {
            createBackup(name);
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

    public void saveStorageData(String guildName, ItemStack[] contents) {
        File storageFile = new File(guildsFolder, guildName + "-storage.yml");
        YamlConfiguration config = new YamlConfiguration();
        config.set("contents", contents);
        try {
            config.save(storageFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save storage for guild: " + guildName);
            e.printStackTrace();
        }
    }

    public ConfigurationSection getStorageData(String guildName) {
        File storageFile = new File(guildsFolder, guildName + "-storage.yml");
        if (storageFile.exists()) {
            return YamlConfiguration.loadConfiguration(storageFile);
        }
        return null;
    }
    private File backupSettingsFile;
    private YamlConfiguration backupSettings;

    private void initBackupSettings() {
        backupSettingsFile = new File(plugin.getDataFolder(), "backup-settings.yml");
        if (!backupSettingsFile.exists()) {
            backupSettings = new YamlConfiguration();
            backupSettings.set("cleanup-interval-days", 30);
            backupSettings.set("last-cleanup", 0);
            try {
                backupSettings.save(backupSettingsFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Could not create backup-settings.yml");
            }
        }
        backupSettings = YamlConfiguration.loadConfiguration(backupSettingsFile);
    }

    private void startBackupCleaner() {
        long lastCleanup = backupSettings.getLong("last-cleanup", 0);
        long currentTime = System.currentTimeMillis();
        long cleanupInterval = backupSettings.getLong("cleanup-interval-days", 30) * 24L * 60L * 60L * 1000L;

        if (currentTime - lastCleanup >= cleanupInterval) {
            plugin.getLogger().info("Starting backup cleanup...");
            cleanupOldBackups(backupSettings.getInt("cleanup-interval-days", 30));
            backupSettings.set("last-cleanup", currentTime);
            try {
                backupSettings.save(backupSettingsFile);
            } catch (IOException e) {
                plugin.getLogger().warning("Could not save backup-settings.yml");
            }
            plugin.getLogger().info("Backup cleanup completed!");
        }


        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            long now = System.currentTimeMillis();
            long lastRun = backupSettings.getLong("last-cleanup");
            long interval = backupSettings.getLong("cleanup-interval-days", 30) * 24L * 60L * 60L * 1000L;

            if (now - lastRun >= interval) {
                plugin.getLogger().info("Starting backup cleanup...");
                cleanupOldBackups(backupSettings.getInt("cleanup-interval-days", 30));
                backupSettings.set("last-cleanup", now);
                try {
                    backupSettings.save(backupSettingsFile);
                } catch (IOException e) {
                    plugin.getLogger().warning("Could not save backup-settings.yml");
                }
                plugin.getLogger().info("Backup cleanup completed!");
            }
        // Check every hour (60 minutes * 60 seconds * 20 ticks)
        }, 72000L, 72000L);
    }
}
