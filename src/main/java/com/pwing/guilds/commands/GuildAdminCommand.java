package com.pwing.guilds.commands;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;


public class GuildAdminCommand implements CommandExecutor {
    private final PwingGuilds plugin;

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
            case "info" -> {
                if (args.length < 2) {
                    sender.sendMessage("§cUsage: /guildadmin info <guild>");
                    return true;
                }
                String guildName = args[1];
                plugin.getGuildManager().getGuild(guildName).ifPresent(guild -> {
                    sender.sendMessage("§6=== Guild Info: " + guild.getName() + " ===");
                    sender.sendMessage("§eLevel: §7" + guild.getLevel());
                    sender.sendMessage("§eMembers: §7" + guild.getMembers().size());
                    sender.sendMessage("§eClaims: §7" + guild.getClaimedChunks().size());
                    sender.sendMessage("§eBonus Claims: §7" + guild.getBonusClaims());
                });
            }
            default -> sendAdminHelp(sender);
        }
        return true;
    }

    private void sendAdminHelp(CommandSender sender) {
        sender.sendMessage("§6=== Guild Admin Commands ===");
        sender.sendMessage("§e/guildadmin addclaims <guild> <amount> §7- Add bonus claims");
        sender.sendMessage("§e/guildadmin setlevel <guild> <level> §7- Set guild level");
        sender.sendMessage("§e/guildadmin delete <guild> §7- Delete a guild");
        sender.sendMessage("§e/guildadmin info <guild> §7- View detailed guild info");
    }
}