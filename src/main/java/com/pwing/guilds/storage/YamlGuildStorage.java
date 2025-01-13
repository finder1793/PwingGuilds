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
import java.util.logging.Level;

/**
 * YAML storage implementation for guilds.
 * Handles saving and loading guild data to and from YAML files.
 */
public class YamlGuildStorage implements GuildStorage {
    private final PwingGuilds plugin;
    private final File guildsFolder;
    private final Map<String, Guild> guildCache = new HashMap<>();
    private static final long AUTO_SAVE_INTERVAL = 6000L;
    private final GuildManager guildManager;

    /**
     * Constructs a new YamlGuildStorage.
     * 
     * @param plugin The PwingGuilds plugin instance.
     */
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

        // New sections
        config.set("pvp-enabled", guild.isPvPEnabled());
        config.set("builtStructures", new HashSet<>(guild.getBuiltStructures()));

        try {
            config.save(guildFile);
            guildCache.put(guild.getName(), guild);
            createBackup(guild.getName());
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save guild: " + guild.getName());
            e.printStackTrace();
        }
    }

    private Map<String, Object> configSectionToMap(ConfigurationSection section) {
        Map<String, Object> map = new HashMap<>();
        for (String key : section.getKeys(false)) {
            Object value = section.get(key);
            if (value instanceof ConfigurationSection) {
                map.put(key, configSectionToMap((ConfigurationSection) value));
            } else {
                map.put(key, value);
            }
        }
        return map;
    }

    private Guild loadGuild(File file) {
        try {
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            String name = config.getString("name");
            String ownerString = config.getString("owner");
            
            if (name == null || ownerString == null) {
                plugin.getLogger().warning("Invalid guild file (missing name or owner): " + file.getName());
                return null;
            }

            Map<String, Object> data = configSectionToMap(config);
            return Guild.deserialize(plugin, data);
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading guild from file: " + file.getName());
            e.printStackTrace();
            return null;
        }
    }

    private boolean needsMigration(YamlConfiguration config) {
        // Check if the file uses the old format
        return config.contains("claimed-chunks") && !config.contains("claims");
    }

    private YamlConfiguration migrateGuildData(YamlConfiguration oldConfig) {
        YamlConfiguration newConfig = new YamlConfiguration();
        
        // Copy basic data
        newConfig.set("name", oldConfig.getString("name"));
        newConfig.set("owner", oldConfig.getString("owner"));
        newConfig.set("level", oldConfig.getInt("level"));
        newConfig.set("exp", oldConfig.getLong("exp", 0L));
        newConfig.set("bonus-claims", oldConfig.getInt("bonus-claims", 0));
        
        // Migrate members
        newConfig.set("members", oldConfig.getStringList("members"));
        
        // Migrate invites if they exist
        if (oldConfig.contains("invites")) {
            newConfig.set("invites", oldConfig.getStringList("invites"));
        }

        // Convert old claimed chunks format to new format
        @SuppressWarnings("unchecked")
        List<Map<?, ?>> oldChunks = oldConfig.getMapList("claimed-chunks");
        List<String> newChunks = new ArrayList<>();
        
        for (Map<?, ?> chunk : oldChunks) {
            String world = String.valueOf(chunk.get("world"));
            int x = Integer.parseInt(String.valueOf(chunk.get("x")));
            int z = Integer.parseInt(String.valueOf(chunk.get("z")));
            newChunks.add(world + "," + x + "," + z);
        }
        newConfig.set("claims", newChunks);

        // Migrate homes if they exist
        if (oldConfig.contains("homes")) {
            newConfig.set("homes", oldConfig.getConfigurationSection("homes"));
        }

        // Migrate alliance if it exists
        if (oldConfig.contains("alliance")) {
            newConfig.set("alliance", oldConfig.getString("alliance"));
        }

        return newConfig;
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
            if (parts.length >= 3) {
                String worldName = parts[0];
                if (worldName == null || Bukkit.getWorld(worldName) == null) {
                    plugin.getLogger().warning("Invalid world name in claim: " + claimString);
                    continue;
                }
                ChunkLocation claim = new ChunkLocation(worldName, Integer.parseInt(parts[1]), Integer.parseInt(parts[2]));
                guild.claimChunk(claim);
            }
        }

        // Load homes if they exist
        ConfigurationSection homesSection = config.getConfigurationSection("homes");
        if (homesSection != null) {
            for (String homeName : homesSection.getKeys(false)) {
                String locationString = homesSection.getString(homeName);
                if (locationString != null) {
                    String[] parts = locationString.split(",");
                    if (parts.length >= 6) {
                        String worldName = parts[0];
                        if (worldName == null || Bukkit.getWorld(worldName) == null) {
                            plugin.getLogger().warning("Invalid world name in home: " + homeName);
                            continue;
                        }
                        Location loc = new Location(
                                Bukkit.getWorld(worldName),
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
        }

        // Check for and add missing sections
        updateGuildConfig(config, guild);

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

    @Override
    public Set<Guild> loadAllGuilds() {
        Set<Guild> guilds = new HashSet<>();
        
        // Ensure guilds folder exists
        if (!guildsFolder.exists()) {
            guildsFolder.mkdirs();
            plugin.getLogger().info("Created guilds directory");
            return guilds; // Return empty set if no guilds exist yet
        }

        // Get only .yml files and filter out backup/storage files
        File[] files = guildsFolder.listFiles((dir, name) -> 
            name.endsWith(".yml") && 
            !name.contains("backup") && 
            !name.contains("-storage")
        );

        if (files != null) {
            for (File file : files) {
                try {
                    Guild guild = loadGuild(file);
                    if (guild != null) {
                        guilds.add(guild);
                        guildCache.put(guild.getName(), guild);
                        plugin.getLogger().info("Loaded guild: " + guild.getName());
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to load guild from file: " + file.getName());
                    e.printStackTrace();
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

    /**
     * Cleans up old backups that are older than the specified number of days.
     * 
     * @param daysToKeep The number of days to keep backups.
     */
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

    private void updateGuildConfig(YamlConfiguration config, Guild guild) {
        boolean updated = false;

        if (!config.contains("bonus-claims")) {
            config.set("bonus-claims", guild.getBonusClaims());
            updated = true;
        }

        if (!config.contains("pvp-enabled")) {
            config.set("pvp-enabled", guild.isPvPEnabled());
            updated = true;
        }

        if (!config.contains("builtStructures")) {
            config.set("builtStructures", new HashSet<>(guild.getBuiltStructures()));
            updated = true;
        }

        if (updated) {
            try {
                config.save(new File(guildsFolder, guild.getName() + ".yml"));
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Error saving updated guild config for: " + guild.getName(), e);
            }
        }
    }
}
