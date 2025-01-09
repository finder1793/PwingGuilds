package com.pwing.guilds.commands;

import org.bukkit.Bukkit;
import com.pwing.guilds.PwingGuilds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GuildCommandTabCompleter implements TabCompleter {
    private final PwingGuilds plugin;

    public GuildCommandTabCompleter(PwingGuilds plugin) {
        this.plugin = plugin;
    }
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "delete", "claim", "unclaim", "visualize", "invite", 
                               "accept", "kick", "gui", "storage", "sethome", "home", "delhome", "buff")
                    .stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        
        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite", "kick" -> {
                    return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .collect(Collectors.toList());
                }
                case "home", "delhome" -> {
                    if (sender instanceof Player player) {
                        return plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                            .map(guild -> guild.getHomes().keySet().stream()
                                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                                .collect(Collectors.toList()))
                            .orElse(Collections.emptyList());
                    }
                }
            }
        }
        return Collections.emptyList();
    }
}
