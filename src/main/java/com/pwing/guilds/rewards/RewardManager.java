package com.pwing.guilds.rewards;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.contest.guildcontests.GuildEvent;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import io.lumine.mythic.bukkit.MythicBukkit;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.rewards.handlers.*;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.*;

public class RewardManager {
    private final PwingGuilds plugin;
    private final List<RewardHandler> handlers = new ArrayList<>();

    public RewardManager(PwingGuilds plugin) {
        this.plugin = plugin;
        registerHandlers();
    }

    private void registerHandlers() {
        // ...existing handlers...
        handlers.add(new GuildExpRewardHandler());
    }

    public void giveReward(Player player, String reward) {
        String[] parts = reward.split(" ");
        String type = parts[0].toLowerCase();

        switch (type) {
            case "money" -> {
                double amount = Double.parseDouble(parts[1]);
                plugin.getEconomy().depositPlayer(player, amount);
                player.sendMessage("§a+" + amount + " coins!");
            }
            case "mythic" -> {
                String itemName = parts[1];
                MythicBukkit.inst().getItemManager().getItem(itemName).ifPresent(mythicItem -> {
                    org.bukkit.inventory.ItemStack item = MythicBukkit.inst().getItemManager().getItemStack(itemName);
                    player.getInventory().addItem(item);
                    player.sendMessage("§aReceived " + itemName + "!");
                });
            }
            case "command" -> {
                String command = reward.substring(8).replace("%player%", player.getName());
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
            }
            case "claims" -> {
                int amount = Integer.parseInt(parts[1]);
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                    guild.addBonusClaims(amount);
                    player.sendMessage("§aYour guild received " + amount + " bonus claims!");
                });
            }        }
    }

    public void giveEventRewards(GuildEvent event) {
        Map<Guild, Integer> scores = event.getScores();
        if (scores.isEmpty()) return;

        List<Map.Entry<Guild, Integer>> topGuilds = new ArrayList<>(scores.entrySet());
        topGuilds.sort(Map.Entry.<Guild, Integer>comparingByValue().reversed());

        String eventPath = "events." + event.getName() + ".rewards";
        
        // Give rewards to top 3 guilds
        for (int i = 0; i < Math.min(3, topGuilds.size()); i++) {
            Guild guild = topGuilds.get(i).getKey();
            String placement = switch (i) {
                case 0 -> "first-place";
                case 1 -> "second-place";
                case 2 -> "third-place";
                default -> null;
            };
            
            if (placement != null) {
                List<String> rewards = plugin.getConfig().getStringList(eventPath + "." + placement);
                giveRewards(guild, rewards);
            }
        }
    }

    private void giveRewards(Guild guild, List<String> rewards) {
        for (String reward : rewards) {
            boolean handled = false;
            for (RewardHandler handler : handlers) {
                if (handler.handleReward(guild, reward)) {
                    handled = true;
                    break;
                }
            }
            if (!handled) {
                plugin.getLogger().warning("Unknown reward type: " + reward);
            }
        }
    }
}
