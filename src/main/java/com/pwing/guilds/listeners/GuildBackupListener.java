package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.events.GuildCreateEvent;
import com.pwing.guilds.events.GuildDeleteEvent;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.storage.GuildBackupManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;


/**
 * Handles automatic guild data backup events
 * Creates backups when guilds are created/deleted and on server shutdown
 */
public class GuildBackupListener implements Listener {
    private final PwingGuilds plugin;
    private final GuildBackupManager backupManager;

    /**
     * Creates a new backup listener
     * @param plugin The plugin instance
     * @param backupManager The backup manager instance
     */
    public GuildBackupListener(PwingGuilds plugin, GuildBackupManager backupManager) {
        this.plugin = plugin;
        this.backupManager = backupManager;
    }

    /**
     * Creates a backup when a new guild is created
     * @param event The guild create event
     */
    @EventHandler
    public void onGuildCreate(GuildCreateEvent event) {
        Guild guild = event.getGuild();
        plugin.getLogger().info("Creating backup for new guild: " + guild.getName());
        
        backupManager.createBackup(guild, "creation");
    }

    /**
     * Creates a backup before a guild is deleted
     * @param event The guild delete event
     */
    @EventHandler
    public void onGuildDelete(GuildDeleteEvent event) {
        Guild guild = event.getGuild();
        plugin.getLogger().info("Creating backup before deleting guild: " + guild.getName());
        
        backupManager.createBackup(guild, "pre-deletion");
    }

    /**
     * Creates backups of all guilds when the server shuts down
     * @param event The plugin disable event
     */
    @EventHandler
    public void onServerShutdown(PluginDisableEvent event) {
        // Only handle our plugin's disable event
        if (!event.getPlugin().equals(plugin)) {
            return;
        }

        plugin.getLogger().info("Creating shutdown backups for all guilds...");
        
        // Backup all active guilds
        plugin.getGuildManager().getGuilds().forEach(guild -> {
            try {
                backupManager.createBackup(guild, "shutdown");
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to create shutdown backup for guild: " + guild.getName());
                e.printStackTrace();
            }
        });

        plugin.getLogger().info("Guild backup process completed!");
    }
}
