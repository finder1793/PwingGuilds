package com.pwing.guilds.alliance.listener;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.Objects;
import java.util.Optional;

/**
 * Listens to and handles alliance-related events.
 * Processes chat events and other alliance interactions.
 */
public class AllianceListener implements Listener {
    private final PwingGuilds plugin;

    /**
     * Creates a new AllianceListener instance
     * @param plugin The PwingGuilds plugin instance
     */
    public AllianceListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles alliance chat messages
     * @param event The chat event
     */
    @EventHandler
    public void onAllianceChat(AsyncPlayerChatEvent event) {
        if (!event.getMessage().startsWith("@a ")) {
            return;
        }

        Player player = event.getPlayer();
        Optional<Guild> playerGuild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());

        if (playerGuild.isEmpty()) {
            player.sendMessage("You must be in a guild to use alliance chat!");
            event.setCancelled(true);
            return;
        }

        Guild guild = playerGuild.get();
        if (guild.getAlliance() == null) {
            player.sendMessage("Your guild is not in an alliance!");
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);
        String message = event.getMessage().substring(3);

        // Format and broadcast alliance chat message
        String formattedMessage = ChatColor.BLUE + "[Alliance] " +
                                ChatColor.GRAY + guild.getName() + " " +
                                ChatColor.WHITE + player.getName() + ": " +
                                ChatColor.YELLOW + message;

        // Broadcast to all online alliance members
        guild.getAlliance().getMembers().forEach(allyGuild ->
            allyGuild.getMembers().stream()
                .map(Bukkit::getPlayer)
                .filter(Objects::nonNull)
                .filter(Player::isOnline)
                .forEach(p -> p.sendMessage(formattedMessage))
        );
    }
}
