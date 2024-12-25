package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class GuildChatListener implements Listener {
    private final PwingGuilds plugin;

    public GuildChatListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        if (event.getMessage().startsWith("@g ")) {
            plugin.getGuildManager().getPlayerGuild(event.getPlayer().getUniqueId())
                .ifPresent(guild -> {
                    String message = event.getMessage().substring(3);
                    guild.broadcastMessage("§2[Guild] §a" + event.getPlayer().getName() + ": §f" + message);
                    event.setCancelled(true);
                });
        }
    }
}
