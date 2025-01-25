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
                case "guild":
                    plugin.getChatManager().setChatMode(player, ChatMode.GUILD);
                    break;
                case "alliance":
                    plugin.getChatManager().setChatMode(player, ChatMode.ALLIANCE);
                    break;
                case "general":
                    plugin.getChatManager().setChatMode(player, ChatMode.GENERAL);
                    break;
                case "spy":
                    if (player.hasPermission("guilds.command.guildchat.spy")) {
                        plugin.getChatManager().toggleSocialSpy(player);
                    } else {
                        player.sendMessage("You do not have permission to use this command.");
                    }
                    break;
                default:
                    player.sendMessage("Invalid chat type. Use 'guild', 'alliance', 'general', or 'spy'.");
                    break;
            }
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        switch (chatType.toLowerCase()) {
            case "guild":
                plugin.getChatManager().sendGuildChat(player, message);
                break;
            case "alliance":
                plugin.getChatManager().sendAllianceChat(player, message);
                break;
            default:
                player.sendMessage("Invalid chat type. Use 'guild' or 'alliance'.");
                break;
        }

        return true;
    }
}
