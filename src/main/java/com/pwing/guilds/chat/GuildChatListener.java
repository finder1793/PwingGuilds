package com.pwing.guilds.chat;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.ChatColor;
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
        if (!event.getMessage().startsWith("@g")) {
            return;
        }

        event.setCancelled(true);
        plugin.getGuildManager().getPlayerGuild(event.getPlayer().getUniqueId()).ifPresent(guild -> {
            String message = event.getMessage().substring(2).trim();
            String format = ChatColor.BLUE + "[Guild] " + ChatColor.GRAY + event.getPlayer().getName() + ": " + ChatColor.WHITE + message;
            
            guild.getMembers().stream()
                    .map(plugin.getServer()::getPlayer)
                    .filter(player -> player != null && player.isOnline())
                    .forEach(player -> player.sendMessage(format));
        });
    }
}
