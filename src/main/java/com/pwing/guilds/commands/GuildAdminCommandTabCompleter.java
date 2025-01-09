package com.pwing.guilds.commands;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GuildAdminCommandTabCompleter implements TabCompleter {
    private final PwingGuilds plugin;

    public GuildAdminCommandTabCompleter(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("guilds.admin")) {
            return Collections.emptyList();
        }

        if (args.length == 1) {
            return Arrays.asList("storage", "addclaims", "setlevel", "delete", "info")
                    .stream()
                    .filter(cmd -> cmd.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return plugin.getGuildManager().getGuilds().stream()
                    .map(guild -> guild.getName())
                    .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3) {
            switch (args[0].toLowerCase()) {
                case "addclaims", "setlevel" -> {
                    return Arrays.asList("1", "5", "10", "25", "50", "100")
                            .stream()
                            .filter(num -> num.startsWith(args[2]))
                            .collect(Collectors.toList());
                }
            }
        }
        return Collections.emptyList();
    }
}
