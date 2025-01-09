package com.pwing.guilds.commands.alliance;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class AllianceCommandTabCompleter implements TabCompleter {
    private final PwingGuilds plugin;

    public AllianceCommandTabCompleter(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("create", "invite", "accept", "decline", "info", "ally")
                    .stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "invite" -> {
                    return plugin.getGuildManager().getGuilds().stream()
                            .map(guild -> guild.getName())
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
                case "accept", "decline", "info" -> {
                    return plugin.getAllianceManager().getAllAlliances().stream()
                            .map(alliance -> alliance.getName())
                            .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
                case "ally" -> {
                    return Arrays.asList("add", "remove")
                            .stream()
                            .filter(cmd -> cmd.toLowerCase().startsWith(args[1].toLowerCase()))
                            .collect(Collectors.toList());
                }
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("ally")) {
            return plugin.getAllianceManager().getAllAlliances().stream()
                    .map(alliance -> alliance.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return Collections.emptyList();
    }
}
