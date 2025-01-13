package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/**
 * Listener for guild perk-related events.
 */
public class GuildPerkListener implements Listener {
    private final PwingGuilds plugin;

    /**
     * Constructs a new GuildPerkListener.
     * @param plugin The plugin instance.
     */
    public GuildPerkListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player death events.
     * @param event The player death event.
     */
    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        plugin.getGuildManager().getPlayerGuild(event.getEntity().getUniqueId())
                .ifPresent(guild -> {
                    if (guild.getPerks().hasKeepInventory()) {
                        event.setKeepInventory(true);
                        event.getDrops().clear();
                        event.getEntity().sendMessage("§aYour guild's keep-inventory perk saved your items!");
                    }
                });
    }
}
