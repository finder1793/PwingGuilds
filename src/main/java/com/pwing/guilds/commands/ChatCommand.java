package com.pwing.guilds.commands;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.chat.ChatManager.ChatMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Handles guild chat and alliance chat commands.
 */
public class ChatCommand implements CommandExecutor {
    private final PwingGuilds plugin;

    /**
     * Constructor for ChatCommand.
     * @param plugin the PwingGuilds plugin instance
     */
    public ChatCommand(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        Player player = (Player) sender;
        if (args.length < 1) {
            player.sendMessage("Usage: /guildchat <guild|alliance|general|spy> [message]");
            return true;
        }

        String chatType = args[0];
        if (args.length == 1) {
            switch (chatType.toLowerCase()) {
                case "guild" -> {
                    if (!player.hasPermission("guilds.command.guildchat.guild")) {
                        player.sendMessage("You do not have permission to use this command.");
                        return true;
                    }
                    plugin.getChatManager().setChatMode(player, ChatMode.GUILD);
                }
                case "alliance" -> {
                    if (!player.hasPermission("guilds.command.guildchat.alliance")) {
                        player.sendMessage("You do not have permission to use this command.");
                        return true;
                    }
                    plugin.getChatManager().setChatMode(player, ChatMode.ALLIANCE);
                }
                case "general" -> {
                    if (!player.hasPermission("guilds.command.guildchat.general")) {
                        player.sendMessage("You do not have permission to use this command.");
                        return true;
                    }
                    plugin.getChatManager().setChatMode(player, ChatMode.GENERAL);
                }
                case "spy" -> {
                    if (!player.hasPermission("guilds.command.guildchat.spy")) {
                        player.sendMessage("You do not have permission to use this command.");
                        return true;
                    }
                    plugin.getChatManager().toggleSocialSpy(player);
                }
                default -> player.sendMessage("Invalid chat type. Use 'guild', 'alliance', 'general', or 'spy'.");
            }
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        switch (chatType.toLowerCase()) {
            case "guild" -> {
                if (!player.hasPermission("guilds.command.guildchat.guild")) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                plugin.getChatManager().sendGuildChat(player, message);
            }
            case "alliance" -> {
                if (!player.hasPermission("guilds.command.guildchat.alliance")) {
                    player.sendMessage("You do not have permission to use this command.");
                    return true;
                }
                plugin.getChatManager().sendAllianceChat(player, message);
            }
            default -> player.sendMessage("Invalid chat type. Use 'guild' or 'alliance'.");
        }

        return true;
    }
}
