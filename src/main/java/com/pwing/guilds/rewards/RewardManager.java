package com.pwing.guilds.rewards;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import io.lumine.mythic.bukkit.MythicBukkit;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.events.custom.GuildEvent;
import java.util.Map;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class RewardManager {
    private final PwingGuilds plugin;

    public RewardManager(PwingGuilds plugin) {
        this.plugin = plugin;
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
        }
    }

    public void giveEventRewards(GuildEvent event) {
        List<Map.Entry<Guild, Integer>> topGuilds = event.getScores().entrySet().stream()
                .sorted(Map.Entry.<Guild, Integer>comparingByValue().reversed())
                .limit(3)
                .collect(Collectors.toList());

        for (int i = 0; i < topGuilds.size(); i++) {
            String placement;
            switch (i) {
                case 0:
                    placement = "first-place";
                    break;
                case 1:
                    placement = "second-place";
                    break;
                case 2:
                    placement = "third-place";
                    break;
                default:
                    continue;
            }

            Guild guild = topGuilds.get(i).getKey();
            List<String> rewards = plugin.getConfig().getStringList("events." + event.getName() + ".rewards." + placement);

            for (UUID memberId : guild.getMembers()) {
                Player player = Bukkit.getPlayer(memberId);
                if (player != null && player.isOnline()) {
                    for (String reward : rewards) {
                        giveReward(player, reward);
                    }
                }
            }
        }
    }
}
