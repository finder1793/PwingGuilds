package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class GuildChatListener implements Listener {
    private final PwingGuilds plugin;
    private final Set<UUID> guildChatMode = new HashSet<>();

    public GuildChatListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (guildChatMode.contains(player.getUniqueId())) {
            event.setCancelled(true);
            plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                String message = "§2[Guild] §a" + player.getName() + ": §f" + event.getMessage();
                guild.getOnlineMembers().forEach(member -> member.sendMessage(message));
            });
        }
    }

    public void toggleGuildChat(UUID playerId) {
        if (guildChatMode.contains(playerId)) {
            guildChatMode.remove(playerId);
        } else {
            guildChatMode.add(playerId);
        }
    }
}
