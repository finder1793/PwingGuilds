package com.pwing.guilds.chat;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.alliance.Alliance;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.ChatColor;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;

/**
 * Manages chat input for players.
 */
public class ChatManager implements Listener {
    private final Map<Player, Consumer<AsyncPlayerChatEvent>> responseHandlers = new HashMap<>();
    private final Map<UUID, ChatMode> playerChatModes = new HashMap<>();
    private final Set<UUID> socialSpyPlayers = new HashSet<>();
    private final Map<UUID, Boolean> pendingTagChanges = new HashMap<>();
    private final PwingGuilds plugin;

    public ChatManager(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers a response handler for a player.
     * @param player The player to register the handler for
     * @param handler The handler to call when the player sends a chat message
     */
    public void expectResponse(Player player, Consumer<AsyncPlayerChatEvent> handler) {
        responseHandlers.put(player, handler);
    }

    public void addPendingTagChange(Player player) {
        pendingTagChanges.put(player.getUniqueId(), true);
    }

    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();

        if (pendingTagChanges.containsKey(playerId)) {
            event.setCancelled(true);
            String newTag = event.getMessage().trim();
            plugin.getGuildManager().getPlayerGuild(playerId).ifPresent(guild -> {
                guild.setTag(newTag);
                plugin.getGuildManager().getStorage().saveGuild(guild);
                player.sendMessage(ChatColor.GREEN + "Guild tag set to: " + newTag);
            });
            pendingTagChanges.remove(playerId);
        }

        if (responseHandlers.containsKey(player)) {
            event.setCancelled(true);
            Consumer<AsyncPlayerChatEvent> handler = responseHandlers.remove(player);
            handler.accept(event);
            return;
        }

        ChatMode chatMode = playerChatModes.getOrDefault(player.getUniqueId(), ChatMode.GENERAL);
        switch (chatMode) {
            case GUILD:
                event.setCancelled(true);
                sendGuildChat(player, event.getMessage());
                break;
            case ALLIANCE:
                event.setCancelled(true);
                sendAllianceChat(player, event.getMessage());
                break;
            default:
                // General chat, do nothing
                break;
        }
    }

    /**
     * Sends a message to all members of the player's guild.
     * @param player The player sending the message
     * @param message The message to send
     */
    public void sendGuildChat(Player player, String message) {
        Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).orElse(null);
        if (guild != null) {
            String formattedMessage = "[Guild] " + player.getName() + ": " + message;
            guild.broadcastMessage(formattedMessage);
            logAndSpy(player, formattedMessage);
        } else {
            player.sendMessage("You are not in a guild.");
        }
    }

    /**
     * Sends a message to all members of the player's alliance.
     * @param player The player sending the message
     * @param message The message to send
     */
    public void sendAllianceChat(Player player, String message) {
        Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).orElse(null);
        if (guild != null) {
            Alliance alliance = guild.getAlliance();
            if (alliance != null) {
                String formattedMessage = "[Alliance] " + player.getName() + ": " + message;
                alliance.getMembers().forEach(memberGuild -> 
                    memberGuild.broadcastMessage(formattedMessage));
                logAndSpy(player, formattedMessage);
            } else {
                player.sendMessage("Your guild is not part of an alliance.");
            }
        } else {
            player.sendMessage("You are not in a guild.");
        }
    }

    /**
     * Logs the message to the console and sends it to social spy players.
     * @param player The player who sent the message
     * @param message The message to log and spy
     */
    private void logAndSpy(Player player, String message) {
        plugin.getLogger().info(message);
        for (UUID spyUUID : socialSpyPlayers) {
            Player spy = Bukkit.getPlayer(spyUUID);
            if (spy != null && spy.isOnline() && !spy.equals(player)) {
                spy.sendMessage("[Spy] " + message);
            }
        }
    }

    /**
     * Toggles the player's chat mode.
     * @param player The player to toggle chat mode for
     * @param mode The chat mode to set
     */
    public void setChatMode(Player player, ChatMode mode) {
        playerChatModes.put(player.getUniqueId(), mode);
        player.sendMessage("Chat mode set to " + mode.name().toLowerCase() + ".");
    }

    /**
     * Gets the player's current chat mode.
     * @param player The player to get the chat mode for
     * @return The current chat mode
     */
    public ChatMode getChatMode(Player player) {
        return playerChatModes.getOrDefault(player.getUniqueId(), ChatMode.GENERAL);
    }

    /**
     * Toggles social spy mode for the player.
     * @param player The player to toggle social spy for
     */
    public void toggleSocialSpy(Player player) {
        UUID playerUUID = player.getUniqueId();
        if (socialSpyPlayers.contains(playerUUID)) {
            socialSpyPlayers.remove(playerUUID);
            player.sendMessage("Social spy disabled.");
        } else {
            socialSpyPlayers.add(playerUUID);
            player.sendMessage("Social spy enabled.");
        }
    }

    public enum ChatMode {
        GENERAL,
        GUILD,
        ALLIANCE
    }
}
