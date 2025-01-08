package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;

public class GuildPlayerListener implements Listener {
    private final PwingGuilds plugin;

    public GuildPlayerListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
            .ifPresent(guild -> {
                plugin.getGuildManager().getPlayerGuilds().put(player.getUniqueId(), guild);
                plugin.getLogger().info("Loaded guild data for " + player.getName());
            });
    }
}
