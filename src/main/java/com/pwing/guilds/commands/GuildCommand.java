package com.pwing.guilds.commands;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.pwing.guilds.visualization.ChunkVisualizer;
import com.pwing.guilds.gui.GuildManagementGUI;

public class GuildCommand implements CommandExecutor {
    private final PwingGuilds plugin;

    public GuildCommand(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("§cOnly players can use guild commands!");
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /guild create <name>");
                    return true;
                }
                if (plugin.getGuildManager().createGuild(args[1], player.getUniqueId())) {
                    player.sendMessage("§aGuild " + args[1] + " created successfully!");
                } else {
                    player.sendMessage("§cA guild with that name already exists!");
                }
            }
            case "claim" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .ifPresentOrElse(
                        playerGuild -> {
                            if (plugin.getGuildManager().claimChunk(playerGuild, player.getLocation().getChunk())) {
                                player.sendMessage("§aChunk claimed successfully!");
                            } else {
                                player.sendMessage("§cFailed to claim chunk!");
                            }
                        },
                        () -> player.sendMessage("§cYou're not in a guild!")
                    );
            }
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /guild invite <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                        .ifPresentOrElse(
                                guild -> {
                                    if (plugin.getGuildManager().invitePlayer(guild.getName(), player.getUniqueId(), target.getUniqueId())) {
                                        player.sendMessage("§aInvited " + target.getName() + " to your guild!");
                                        target.sendMessage("§aYou've been invited to join " + guild.getName() + "! Use /guild accept " + guild.getName() + " to join.");
                                    }
                                },
                                () -> player.sendMessage("§cYou're not in a guild!")
                        );
            }
            case "accept" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /guild accept <guild>");
                    return true;
                }
                if (plugin.getGuildManager().acceptInvite(args[1], player.getUniqueId())) {
                    player.sendMessage("§aYou've joined the guild!");
                } else {
                    player.sendMessage("§cNo pending invite found!");
                }
            }
            case "kick" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /guild kick <player>");
                    return true;
                }
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage("§cPlayer not found!");
                    return true;
                }
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                        .ifPresentOrElse(
                                guild -> {
                                    if (plugin.getGuildManager().kickMember(guild.getName(), player.getUniqueId(), target.getUniqueId())) {
                                        player.sendMessage("§aKicked " + target.getName() + " from the guild!");
                                        target.sendMessage("§cYou've been kicked from " + guild.getName() + "!");
                                    } else {
                                        player.sendMessage("§cYou don't have permission to kick members!");
                                    }
                                },
                                () -> player.sendMessage("§cYou're not in a guild!")
                        );
            }
            case "unclaim" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                        .ifPresentOrElse(
                                guild -> {
                                    if (plugin.getGuildManager().unclaimChunk(guild, player.getLocation().getChunk())) {
                                        player.sendMessage("§aChunk unclaimed successfully!");
                                    } else {
                                        player.sendMessage("§cThis chunk isn't claimed by your guild!");
                                    }
                                },
                                () -> player.sendMessage("§cYou're not in a guild!")
                        );
            }
            
            case "borders" -> {
                plugin.getGuildManager().getGuildByChunk(player.getLocation().getChunk())
                        .ifPresentOrElse(
                                guild -> {
                                    ChunkVisualizer.showChunkBorders(player, player.getLocation().getChunk());
                                    player.sendMessage("§aShowing chunk borders!");
                                },
                                () -> player.sendMessage("§cNo guild claims in this chunk!")
                        );
            }
            case "info" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresentOrElse(
                        guild -> {
                            player.sendMessage("§6=== Guild Info ===");
                            player.sendMessage("§eName: §7" + guild.getName());
                            player.sendMessage("§eLevel: §7" + guild.getLevel());
                            player.sendMessage("§eExp: §7" + guild.getExp());
                            player.sendMessage("§eClaimed Chunks: §7" + guild.getClaimedChunks().size());
                            int maxClaims = plugin.getConfig().getInt("guild-levels." + guild.getLevel() + ".max-claims");
                            player.sendMessage("§eMax Claims: §7" + maxClaims);
                        },
                        () -> player.sendMessage("§cYou're not in a guild!")
                );
            }
            case "gui", "menu" -> {
                new GuildManagementGUI(plugin).openMainMenu(player);
            }

            default -> sendHelpMessage(player);
        }
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== Guild Commands ===");
        player.sendMessage("§e/guild create <name> §7- Create a new guild");
        player.sendMessage("§e/guild claim §7- Claim the chunk you're standing in");
        player.sendMessage("§e/guild unclaim §7- Unclaim the chunk you're standing in");
        player.sendMessage("§e/guild invite <player> §7- Invite a player to your guild");
        player.sendMessage("§e/guild accept <guild> §7- Accept a guild invitation");
        player.sendMessage("§e/guild kick <player> §7- Kick a player from your guild");
        player.sendMessage("§e/guild borders §7- Show the borders of the current chunk");
        player.sendMessage("§e/guild info §7- Display information about your guild");
        player.sendMessage("§e/guild gui §7- Open the guild management interface");

    }
}




