package com.pwing.guilds.commands.alliance;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.guild.Guild;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AllianceCommand implements CommandExecutor {
    private final PwingGuilds plugin;

    public AllianceCommand(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Only players can use alliance commands!");
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create":
                handleCreate(player, args);
                break;
            case "invite":
                handleInvite(player, args);
                break;
            case "accept":
                handleAccept(player, args);
                break;
            case "decline":
                handleDecline(player, args);
                break;
            case "info":
                handleInfo(player, args);
                break;
            case "ally":
                handleAlly(player, args);
                break;
            default:
                sendHelp(player);
                break;
        }
        return true;
    }

    private void handleCreate(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /alliance create <name>");
            return;
        }

        String allianceName = args[1];
        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            if (plugin.getGuildManager().createAlliance(allianceName, guild)) {
                player.sendMessage("Alliance " + allianceName + " created successfully!");
            } else {
                player.sendMessage("Could not create alliance. Name might be taken.");
            }
        });
    }

    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /alliance invite <guild>");
            return;
        }

        String targetGuildName = args[1];
        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            plugin.getGuildManager().getGuild(targetGuildName).ifPresent(targetGuild -> {
                if (plugin.getGuildManager().inviteToAlliance(guild.getAlliance().getName(), guild, targetGuild)) {
                    player.sendMessage("Invited " + targetGuildName + " to your alliance!");
                } else {
                    player.sendMessage("Could not invite guild to alliance.");
                }
            });
        });
    }

    private void handleAccept(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /alliance accept <alliance>");
            return;
        }

        String allianceName = args[1];
        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            plugin.getAllianceManager().getAlliance(allianceName).ifPresent(alliance -> {
                if (alliance.acceptInvite(guild)) {
                    player.sendMessage("Successfully joined alliance " + allianceName + "!");
                } else {
                    player.sendMessage("No pending invite from this alliance.");
                }
            });
        });
    }

    private void handleDecline(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /alliance decline <alliance>");
            return;
        }

        String allianceName = args[1];
        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            plugin.getAllianceManager().getAlliance(allianceName).ifPresent(alliance -> {
                if (alliance.declineInvite(guild)) {
                    player.sendMessage("Declined alliance invite from " + allianceName);
                } else {
                    player.sendMessage("No pending invite from this alliance.");
                }
            });
        });
    }

    private void handleInfo(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /alliance info <alliance>");
            return;
        }

        String allianceName = args[1];
        plugin.getAllianceManager().getAlliance(allianceName).ifPresent(alliance -> {
            player.sendMessage("§6=== Alliance: " + alliance.getName() + " ===");
            player.sendMessage("§eOwner: " + alliance.getOwnerGuild());
            player.sendMessage("§eMembers: " + String.join(", ", 
                alliance.getMembers().stream()
                    .map(Guild::getName)
                    .toArray(String[]::new)));
            player.sendMessage("§eAllies: " + String.join(", ", alliance.getAllies()));
        });
    }

    private void handleAlly(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage("Usage: /alliance ally <add/remove> <alliance>");
            return;
        }

        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            if (guild.getAlliance() == null) {
                player.sendMessage("Your guild is not in an alliance!");
                return;
            }

            if (args.length < 3) {
                player.sendMessage("Usage: /alliance ally <add/remove> <alliance>");
                return;
            }

            String targetAllianceName = args[2];
            if (args[1].equalsIgnoreCase("add")) {
                if (guild.getAlliance().addAlly(targetAllianceName)) {
                    player.sendMessage("Added " + targetAllianceName + " as an ally!");
                }
            } else if (args[1].equalsIgnoreCase("remove")) {
                if (guild.getAlliance().removeAlly(targetAllianceName)) {
                    player.sendMessage("Removed " + targetAllianceName + " from allies!");
                }
            }
        });
    }

    private void sendHelp(Player player) {
        player.sendMessage("§6=== Alliance Commands ===");
        player.sendMessage("§e/alliance create <name> - Create a new alliance");
        player.sendMessage("§e/alliance invite <guild> - Invite a guild to your alliance");
        player.sendMessage("§e/alliance accept <alliance> - Accept an alliance invite");
        player.sendMessage("§e/alliance decline <alliance> - Decline an alliance invite");
        player.sendMessage("§e/alliance info <alliance> - View alliance information");
        player.sendMessage("§e/alliance ally <add/remove> <alliance> - Manage alliance relationships");
    }
}
