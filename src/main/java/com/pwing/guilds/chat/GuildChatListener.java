package com.pwing.guilds.chat;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * Listens for and handles guild chat messages.
 * Processes chat formatting and routing of guild messages.
 * Handles guild chat functionality and message formatting.
 * Intercepts chat messages starting with "@g" and routes them to guild members.
 */
public class GuildChatListener implements Listener {
    private final PwingGuilds plugin;

    /**
     * Creates a new guild chat listener
     * @param plugin The plugin instance
     */
    public GuildChatListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player chat events for guild chat
     * @param event The chat event
     * @throws IllegalStateException if message routing fails
     */
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
