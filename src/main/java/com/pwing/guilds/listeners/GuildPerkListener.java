package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

public class GuildPerkListener implements Listener {
    private final PwingGuilds plugin;

    public GuildPerkListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

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
