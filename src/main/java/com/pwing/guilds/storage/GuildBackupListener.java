package com.pwing.guilds.storage;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.events.GuildCreateEvent;
import com.pwing.guilds.events.GuildDeleteEvent;
import com.pwing.guilds.storage.GuildBackupManager;


public class GuildBackupListener implements Listener {
    private final PwingGuilds plugin;
    private final GuildBackupManager backupManager;

    public GuildBackupListener(PwingGuilds plugin, GuildBackupManager backupManager) {
        this.plugin = plugin;
        this.backupManager = backupManager;
    }

    @EventHandler
    public void onGuildCreate(GuildCreateEvent event) {
        if (plugin.getConfig().getBoolean("backup.auto-backup.on-guild-create")) {
            backupManager.createCompressedBackup(event.getGuild());
        }
    }

    @EventHandler
    public void onGuildDelete(GuildDeleteEvent event) {
        if (plugin.getConfig().getBoolean("backup.auto-backup.on-guild-delete")) {
            backupManager.createCompressedBackup(event.getGuild());
        }
    }

    @EventHandler
    public void onServerShutdown(PluginDisableEvent event) {
        if (event.getPlugin().equals(plugin) && plugin.getConfig().getBoolean("backup.auto-backup.on-server-shutdown")) {
            backupManager.backupAllGuilds();
        }
    }
}
