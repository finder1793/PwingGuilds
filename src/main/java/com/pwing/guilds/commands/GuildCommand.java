package com.pwing.guilds.commands;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import com.pwing.guilds.message.MessageManager;
import org.bukkit.entity.Player;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildHome;
import com.pwing.guilds.visualization.ChunkVisualizer;
import com.pwing.guilds.gui.GuildManagementGUI;
import com.pwing.guilds.gui.StructureManagementGUI;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles all guild-related commands.
 * Manages guild creation, deletion, invites, claims, and other guild operations.
 */
public class GuildCommand implements CommandExecutor {
    private final PwingGuilds plugin;

    /**
     * Constructs a new GuildCommand instance.
     * @param plugin The main plugin instance
     */
    public GuildCommand(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("guilds.command.guild")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("general.no-permission"));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.getMessageManager().getMessage("general.player-only"));
            return true;
        }

        if (args.length == 0) {
            sendHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "create" -> handleCreate(player, args);
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
                        guild -> {
                            if (guild.isChunkClaimed(player.getLocation().getChunk())) {
                                player.sendMessage("§cThis chunk is already claimed!");
                                player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                return;
                            }
                            if (plugin.getGuildManager().claimChunk(guild, player.getLocation().getChunk())) {
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
            }            case "visualize", "show" -> {
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
            case "unclaim" -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                        .ifPresentOrElse(
                                guild -> {
                                    if (plugin.getGuildManager().unclaimChunk(guild, player.getLocation().getChunk())) {
                                        player.sendMessage("§aChunk unclaimed successfully!");
                                        ChunkVisualizer.showChunkBorders(player, player.getLocation().getChunk());
                                        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
                                    } else {
                                        player.sendMessage("§cThis chunk is not claimed by your guild!");
                                        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                    }
                                },
                                () -> {
                                    player.sendMessage("§cYou're not in a guild!");
                                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                                }
                        );
            }
            case "paste" -> {
                if (!(sender instanceof Player)) {
                    sender.sendMessage("Only players can use this command.");
                    return true;
                }

                if (args.length < 2) {
                    player.sendMessage("Usage: /guild paste <schematic>");
                    return true;
                }

                String schematicName = args[1];
                Guild guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).orElse(null);
                if (guild == null) {
                    player.sendMessage("You are not in a guild.");
                    return true;
                }

                if (!plugin.getGuildManager().canPasteSchematic(guild, player, schematicName)) {
                    player.sendMessage("You do not have the required materials to paste this schematic.");
                    return true;
                }

                plugin.getGuildManager().pasteSchematic(guild, player, schematicName);
                player.sendMessage("Schematic pasted successfully.");
                return true;
            }
            case "structures" -> {
                if (!plugin.isAllowStructures()) {
                    player.sendMessage("The structure system is currently disabled.");
                    return true;
                }

                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresentOrElse(
                    guild -> {
                        if (!guild.getLeader().equals(player.getUniqueId())) {
                            player.sendMessage("Only the guild leader can manage structures.");
                            return;
                        }
                        new StructureManagementGUI(plugin).openStructureMenu(player, guild);
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
        MessageManager mm = plugin.getMessageManager();
        player.sendMessage(mm.getMessage("commands.guild.help.header"));
        for (String key : Arrays.asList("create", "delete", "claim", "unclaim", "visualize",
                "invite", "accept", "kick", "gui", "storage", "buff", "home", "sethome", "delhome")) {
            player.sendMessage(mm.getMessage("commands.guild.help." + key));
        }
    }

    private void handleCreate(Player player, String[] args) {
        MessageManager mm = plugin.getMessageManager();
        if (args.length < 2) {
            player.sendMessage(mm.getMessage("commands.guild.usage.create"));
            return;
        }
        Map<String, String> replacements = new HashMap<>();
        replacements.put("name", args[1]);
        
        if (plugin.getGuildManager().createGuild(args[1], player.getUniqueId())) {
            player.sendMessage(mm.getMessage("commands.guild.success.created", replacements));
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
        } else {
            player.sendMessage(mm.getMessage("error.guild-exists"));
        }
    }

}


