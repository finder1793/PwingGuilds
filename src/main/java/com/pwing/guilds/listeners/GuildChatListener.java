package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Handles guild chat functionality and message routing
 * Routes messages prefixed with "@g" to guild members
 */
public class GuildChatListener implements Listener {
    private final PwingGuilds plugin;
    private final Set<UUID> guildChatEnabled = new HashSet<>();

    /**
     * Creates a new guild chat listener
     * @param plugin The plugin instance
     */
    public GuildChatListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles player chat events for guild chat
     * Routes messages to appropriate guild members
     * @param event The chat event
     */
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (guildChatEnabled.contains(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                String message = "§2[Guild] §a" + player.getName() + ": §f" + event.getMessage();
                guild.getOnlineMembers().forEach(member -> member.sendMessage(message));
            });
        }
    }

    /**
     * Toggles guild chat mode for a player
     * @param playerId UUID of player to toggle chat for
     */
    public void toggleGuildChat(UUID playerId) {
        if (guildChatEnabled.contains(playerId)) {
            guildChatEnabled.remove(playerId);
        } else {
            guildChatEnabled.add(playerId);
        }
    }
}
