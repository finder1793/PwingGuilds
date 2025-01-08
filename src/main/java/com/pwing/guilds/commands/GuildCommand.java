package com.pwing.guilds.commands;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildHome;
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
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                } else {
                    player.sendMessage("§cA guild with that name already exists!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            }
            case "delete" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                    if (guild.getLeader().equals(player.getUniqueId())) {
                        plugin.getGuildManager().deleteGuild(guild.getName());
                        player.sendMessage("§aGuild deleted successfully!");
                        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_DEATH, 1.0f, 1.0f);
                    } else {
                        player.sendMessage("§cOnly the guild leader can delete the guild!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                });
            }
            case "claim" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .ifPresentOrElse(
                        playerGuild -> {
                            if (plugin.getGuildManager().claimChunk(playerGuild, player.getLocation().getChunk())) {
                                player.sendMessage("§aChunk claimed successfully!");
                                ChunkVisualizer.showChunkBorders(player, player.getLocation().getChunk());
                                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                            } else {
                                player.sendMessage("§cFailed to claim chunk!");
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            }
                        },
                        () -> {
                            player.sendMessage("§cYou're not in a guild!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                    );
            }
            case "visualize", "show" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .ifPresentOrElse(
                        guild -> {
                            ChunkVisualizer.showChunkBorders(player, player.getLocation().getChunk());
                            player.sendMessage("§aShowing chunk borders!");
                            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                        },
                        () -> {
                            player.sendMessage("§cYou're not in a guild!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
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
                                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                        target.playSound(target.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
                                    }
                                },
                                () -> {
                                    player.sendMessage("§cYou're not in a guild!");
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                }
                        );
            }
            case "accept" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /guild accept <guild>");
                    return true;
                }
                if (plugin.getGuildManager().acceptInvite(args[1], player.getUniqueId())) {
                    player.sendMessage("§aYou've joined the guild!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                } else {
                    player.sendMessage("§cNo pending invite found!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
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
                                        player.playSound(player.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f);
                                        target.playSound(target.getLocation(), Sound.ENTITY_IRON_GOLEM_HURT, 1.0f, 1.0f);
                                    } else {
                                        player.sendMessage("§cYou don't have permission to kick members!");
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                    }
                                },
                                () -> {
                                    player.sendMessage("§cYou're not in a guild!");
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                }
                        );
            }
            case "gui", "menu" -> {
                new GuildManagementGUI(plugin).openMainMenu(player);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            }
            case "storage", "chest" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresentOrElse(
                    guild -> {
                        if (guild.getPerks().activatePerk("guild-storage")) {
                            plugin.getStorageManager().openStorage(player, guild);
                            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                        } else {
                            player.sendMessage("§cYour guild doesn't have storage access yet!");
                            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                        }
                    },
                    () -> {
                        player.sendMessage("§cYou're not in a guild!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                );
            }
            case "sethome" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /guild sethome <name>");
                    return true;
                }
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                    if (guild.setHome(args[1], player.getLocation())) {
                        player.sendMessage("§aGuild home '" + args[1] + "' set!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                    } else {
                        player.sendMessage("§cYou've reached your guild's home limit!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                });
            }
            case "home" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /guild home <name>");
                    return true;
                }
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                    guild.getHome(args[1]).ifPresentOrElse(
                            home -> {
                                player.teleport(home.getLocation());
                                player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
                            },
                            () -> {
                                player.sendMessage("§cHome '" + args[1] + "' not found!");
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                            }
                    );
                });
            }
            case "delhome" -> {
                if (args.length < 2) {
                    player.sendMessage("§cUsage: /guild delhome <name>");
                    return true;
                }
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                    if (guild.deleteHome(args[1])) {
                        player.sendMessage("§aGuild home '" + args[1] + "' deleted!");
                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1.0f, 1.0f);
                    } else {
                        player.sendMessage("§cHome '" + args[1] + "' not found!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                });
            }
            case "buff" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresentOrElse(
                    guild -> {
                        new GuildManagementGUI(plugin).openBuffsMenu(player, guild);
                        player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
                    },
                    () -> {
                        player.sendMessage("§cYou're not in a guild!");
                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    }
                );
            }
            default -> sendHelpMessage(player);
        }
        return true;
    }

    private void sendHelpMessage(Player player) {
        player.sendMessage("§6=== Guild Commands ===");
        player.sendMessage("§e/guild create <name> §7- Create a new guild");
        player.sendMessage("§e/guild delete §7- Delete your guild");
        player.sendMessage("§e/guild claim §7- Claim the chunk you're standing in");
        player.sendMessage("§e/guild unclaim §7- Unclaim the chunk you're standing in");
        player.sendMessage("§e/guild visualize §7- Show chunk borders");
        player.sendMessage("§e/guild invite <player> §7- Invite a player to your guild");
        player.sendMessage("§e/guild accept <guild> §7- Accept a guild invitation");
        player.sendMessage("§e/guild kick <player> §7- Kick a player from your guild");
        player.sendMessage("§e/guild gui §7- Open the guild management interface");
        player.sendMessage("§e/guild sethome <name> §7- Set a guild home");
        player.sendMessage("§e/guild home <name> §7- Teleport to a guild home");
        player.sendMessage("§e/guild delhome <name> §7- Delete a guild home");
        player.sendMessage("§e/guild storage §7- Open guild storage chest");
        player.sendMessage("§e/guild buff §7- Opens Guild Buff Menu");
    }
}
