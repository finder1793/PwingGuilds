package com.pwing.guilds.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import com.pwing.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.Bukkit;
import com.pwing.guilds.PwingGuilds;

/**
 * Handles placeholders for guild-related information.
 */
public class GuildPlaceholders extends PlaceholderExpansion {
private final PwingGuilds plugin;

    /**
     * Constructs a new GuildPlaceholders instance.
     * @param plugin The plugin instance.
     */
    public GuildPlaceholders(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @Override
    public String getIdentifier() {
        return "guild";
    }

    @Override
    public String getAuthor() {
        return "YourName";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public String onPlaceholderRequest(Player player, String identifier) {
        if (player == null) return "";

        return switch (identifier) {
            case "name" -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .map(guild -> guild.getName())
                    .orElse("No Guild");

            case "level" -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .map(guild -> String.valueOf(guild.getLevel()))
                    .orElse("0");

            case "exp" -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .map(guild -> String.valueOf(guild.getExp()))
                    .orElse("0");

            case "members" -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .map(guild -> String.valueOf(guild.getMembers().size()))
                    .orElse("0");

            case "claims" -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .map(guild -> String.valueOf(guild.getClaimedChunks().size()))
                    .orElse("0");

            case "owner" -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .map(guild -> Bukkit.getOfflinePlayer(guild.getOwner()).getName())
                    .orElse("None");

            case "next_level_exp" -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .map(guild -> String.valueOf(plugin.getConfig().getLong("guild-levels." + (guild.getLevel() + 1) + ".exp-required")))
                    .orElse("0");

                case "claim_status" -> {
                    var chunk = player.getLocation().getChunk();
                    var claimingGuild = plugin.getGuildManager().getGuilds().stream()
                        .filter(g -> g.isChunkClaimed(chunk))
                        .findFirst();
                    yield claimingGuild.isPresent() ? "Claimed" : "Unclaimed";
                }

            case "alliance" -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .map(guild -> guild.getAlliance() != null ? guild.getAlliance().getName() : "No Alliance")
                    .orElse("No Guild");

            default -> null;
        };
    }
}