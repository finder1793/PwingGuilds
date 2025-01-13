package com.pwing.guilds.commands;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildManager;
import com.pwing.guilds.message.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles admin commands for managing guilds.
 */
public class GuildAdminCommand implements CommandExecutor {
    private final PwingGuilds plugin;

    /**
     * Creates a new GuildAdminCommand instance.
     * @param plugin The plugin instance.
     */
    public GuildAdminCommand(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("guilds.admin")) {
            sender.sendMessage("§cYou don't have permission to use admin commands!");
            return true;
        }

        if (args.length == 0) {
            sendAdminHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "storage" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /guildadmin storage <guild>");
                    return true;
                }
                if (!(sender instanceof Player player)) {
                    sender.sendMessage("§cOnly players can view guild storage!");
                    return true;
                }
                String guildName = args[1];
                plugin.getGuildManager().getGuild(guildName).ifPresentOrElse(
                    guild -> {
                        plugin.getStorageManager().openStorage(player, guild);
                        sender.sendMessage("§aOpening storage for guild: " + guildName);
                    },
                    () -> sender.sendMessage("§cGuild not found!")
                );
            }
            case "addclaims" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /guildadmin addclaims <guild> <amount>");
                    return true;
                }
                String guildName = args[1];
                try {
                    int amount = Integer.parseInt(args[2]);
                    plugin.getGuildManager().getGuild(guildName).ifPresent(guild -> {
                        guild.addBonusClaims(amount);
                        sender.sendMessage("§aAdded " + amount + " bonus claims to " + guildName);
                    });
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number format!");
                }
            }
            case "setlevel" -> {
                if (args.length < 3) {
                    sender.sendMessage("§cUsage: /guildadmin setlevel <guild> <level>");
                    return true;
                }
                String guildName = args[1];
                try {
                    int level = Integer.parseInt(args[2]);
                    plugin.getGuildManager().getGuild(guildName).ifPresent(guild -> {
                        guild.setLevel(level);
                        sender.sendMessage("§aSet " + guildName + "'s level to " + level);
                    });
                } catch (NumberFormatException e) {
                    sender.sendMessage("§cInvalid number format!");
                }
            }
            case "delete" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /guildadmin delete <guild>");
                    return true;
                }
                String guildName = args[1];
                plugin.getGuildManager().deleteGuild(guildName);
                sender.sendMessage("§aDeleted guild " + guildName);
            }
            case "info" -> handleInfo(sender, args);
            default -> sendAdminHelp(sender);
        }
        return true;
    }

    private void sendAdminHelp(CommandSender sender) {
        MessageManager mm = plugin.getMessageManager();
        sender.sendMessage(mm.getMessage("commands.admin.help.header"));
        for (String key : Arrays.asList("storage", "addclaims", "setlevel", "delete", "info")) {
            sender.sendMessage(mm.getMessage("commands.admin.help." + key));
        }
    }

    private void handleInfo(CommandSender sender, String[] args) {
        MessageManager mm = plugin.getMessageManager();
        if (args.length < 2) {
            sender.sendMessage(mm.getMessage("commands.admin.usage.info"));
            return;
        }

        String guildName = args[1];
        plugin.getGuildManager().getGuild(guildName).ifPresent(guild -> {
            Map<String, String> replacements = new HashMap<>();
            replacements.put("guild", guild.getName());
            replacements.put("level", String.valueOf(guild.getLevel()));
            replacements.put("count", String.valueOf(guild.getMembers().size()));
            replacements.put("claims", String.valueOf(guild.getClaimedChunks().size()));
            replacements.put("bonus", String.valueOf(guild.getBonusClaims()));

            sender.sendMessage(mm.getMessage("commands.admin.info.header", replacements));
            sender.sendMessage(mm.getMessage("commands.admin.info.level", replacements));
            sender.sendMessage(mm.getMessage("commands.admin.info.members", replacements));
            sender.sendMessage(mm.getMessage("commands.admin.info.claims", replacements));
            sender.sendMessage(mm.getMessage("commands.admin.info.bonus-claims", replacements));
        });
    }
}
