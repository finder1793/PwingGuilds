package com.pwing.guilds.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.configuration.InvalidConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import java.util.Comparator;


public class GuildBackupManager {
    private final PwingGuilds plugin;
    private final File backupFolder;
    private final int compressionLevel;
    private final long backupInterval;
    private final int retentionDays;
    private final int minBackups;
    private BukkitTask backupTask;

    public GuildBackupManager(PwingGuilds plugin) {
        this.plugin = plugin;
        this.backupFolder = new File(plugin.getDataFolder(), "backups");
        this.compressionLevel = plugin.getConfig().getInt("backup.compression-level", 9);
        this.backupInterval = plugin.getConfig().getLong("backup.interval", 60) * 1200L; // Convert to ticks
        this.retentionDays = plugin.getConfig().getInt("backup.retention.days", 7);
        this.minBackups = plugin.getConfig().getInt("backup.retention.keep-minimum", 5);

        if (!backupFolder.exists()) {
            backupFolder.mkdirs();
        }

        if (plugin.getConfig().getBoolean("backup.enabled", true)) {
            startScheduledBackups();
        }
    }

    private void startScheduledBackups() {
        backupTask = Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            plugin.getLogger().info("Starting scheduled guild backup...");
            backupAllGuilds();
            cleanupOldBackups();
        }, backupInterval, backupInterval);
    }

    public void backupAllGuilds() {
        for (Guild guild : plugin.getGuildManager().getGuilds()) {
            createCompressedBackup(guild);
        }
    }

    public void createCompressedBackup(Guild guild) {
        String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
        File backupFile = new File(backupFolder, guild.getName() + "-" + timestamp + ".gz");

        try (GZIPOutputStream gzos = new GZIPOutputStream(new FileOutputStream(backupFile)) {
            {
                def.setLevel(compressionLevel);
            }
        }) {
            YamlConfiguration config = new YamlConfiguration();
            config.set("guild", guild.serialize());
            gzos.write(config.saveToString().getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create compressed backup for guild: " + guild.getName());
        }
    }

    public void restoreFromBackup(String backupFileName) {
        File backupFile = new File(backupFolder, backupFileName);
        if (!backupFile.exists()) return;

        try (GZIPInputStream gzis = new GZIPInputStream(new FileInputStream(backupFile))) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzis.read(buffer)) > 0) {
                baos.write(buffer, 0, len);
            }

            String content = baos.toString(StandardCharsets.UTF_8);
            YamlConfiguration config = new YamlConfiguration();
            try {
                config.loadFromString(content);
            } catch (InvalidConfigurationException e) {
                plugin.getLogger().warning("Invalid backup configuration in file: " + backupFileName);
                return;
            }

            // Restore guild data
            Map<String, Object> serializedGuild = config.getConfigurationSection("guild").getValues(true);
            Guild restoredGuild = Guild.deserialize(plugin, serializedGuild);
            plugin.getGuildManager().addGuild(restoredGuild);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to restore backup: " + backupFileName);
        }
    }

    private void cleanupOldBackups() {
        File[] backups = backupFolder.listFiles((dir, name) -> name.endsWith(".gz"));
        if (backups == null || backups.length <= minBackups) return;

        Arrays.sort(backups, Comparator.comparingLong(File::lastModified).reversed());
        long cutoffTime = System.currentTimeMillis() - (retentionDays * 24 * 60 * 60 * 1000L);

        // Keep minimum required backups
        for (int i = minBackups; i < backups.length; i++) {
            if (backups[i].lastModified() < cutoffTime) {
                backups[i].delete();
            }
        }
    }
}


