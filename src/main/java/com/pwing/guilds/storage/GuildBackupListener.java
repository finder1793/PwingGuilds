package com.pwing.guilds.storage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.api.GuildCreateEvent;
import com.pwing.guilds.api.GuildDeleteEvent;
import com.pwing.guilds.storage.GuildBackupManager;

/**
 * Listens for guild-related events to create backups.
 */
public class GuildBackupListener implements Listener {
    private final PwingGuilds plugin;
    private final GuildBackupManager backupManager;

    /**
     * Creates a new GuildBackupListener instance.
     * @param plugin The plugin instance.
     * @param backupManager The backup manager instance.
     */
    public GuildBackupListener(PwingGuilds plugin, GuildBackupManager backupManager) {
        this.plugin = plugin;
        this.backupManager = backupManager;
    }

    /**
     * Creates a backup when a new guild is created.
     * @param event The guild create event.
     */
    @EventHandler
    public void onGuildCreate(GuildCreateEvent event) {
        if (plugin.getConfig().getBoolean("backup.auto-backup.on-guild-create")) {
            backupManager.createCompressedBackup(event.getGuild());
        }
    }

    /**
     * Creates a backup before a guild is deleted.
     * @param event The guild delete event.
     */
    @EventHandler
    public void onGuildDelete(GuildDeleteEvent event) {
        if (plugin.getConfig().getBoolean("backup.auto-backup.on-guild-delete")) {
            backupManager.createCompressedBackup(event.getGuild());
        }
    }

    /**
     * Creates backups of all guilds when the server shuts down.
     * @param event The plugin disable event.
     */
    @EventHandler
    public void onServerShutdown(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin) && plugin.getConfig().getBoolean("backup.auto-backup.on-server-shutdown")) {
            backupManager.backupAllGuilds();
        }
    }
}
