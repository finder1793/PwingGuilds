package com.pwing.guilds.contest.guildcontests;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.MerchantInventory;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Represents the Merchant Festival event in the guild contest.
 */
public class MerchantFestivalEvent extends GuildEvent implements Listener {
    private final Map<Guild, Double> tradingProfit = new HashMap<>();
    private final Map<UUID, ItemStack[]> lastTradeItems = new HashMap<>();

    /**
     * Creates a new MerchantFestivalEvent.
     * @param plugin Plugin instance.
     * @param name Event name.
     * @param duration Event duration in seconds.
     */
    public MerchantFestivalEvent(PwingGuilds plugin, String name, int duration) {
        super(plugin, name, duration);
        this.description = "Generate the most profit through trading and commerce!";
    }

    @Override
    public void start() {
        isActive = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.broadcastMessage("§6§lMerchant Festival has begun! Trade for your guild's fortune!");
    }

    @Override
    public void updateScores() {
        tradingProfit.forEach((guild, profit) -> 
            scores.put(guild, (int)(profit * 100)));
    }

    /**
     * Handles the villager trade event during the Merchant Festival.
     * @param event The InventoryClickEvent.
     */
    @EventHandler
    public void onVillagerTrade(InventoryClickEvent event) {
        if (!isActive || !(event.getInventory() instanceof MerchantInventory)) return;
        
        Player player = (Player) event.getWhoClicked();
        Optional<Guild> guild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (!guild.isPresent()) return;

        // Store items before trade
        lastTradeItems.put(player.getUniqueId(), player.getInventory().getContents().clone());

        // Schedule check after trade completes
        Bukkit.getScheduler().runTask(plugin, () -> {
            ItemStack[] before = lastTradeItems.get(player.getUniqueId());
            ItemStack[] after = player.getInventory().getContents();
            
            double profit = calculateTradeProfit(before, after);
            if (profit > 0) {
                tradingProfit.merge(guild.get(), profit, Double::sum);
                player.sendMessage("§6+" + String.format("%.1f", profit) + " trade profit for " + guild.get().getName());
            }
        });
    }

    private double calculateTradeProfit(ItemStack[] before, ItemStack[] after) {
        double totalValue = 0;
        Map<Material, Integer> beforeCount = countItems(before);
        Map<Material, Integer> afterCount = countItems(after);

        for (Material material : afterCount.keySet()) {
            int difference = afterCount.get(material) - beforeCount.getOrDefault(material, 0);
            if (difference > 0) {
                totalValue += getItemValue(material) * difference;
            }
        }
        return totalValue;
    }

    private Map<Material, Integer> countItems(ItemStack[] items) {
        Map<Material, Integer> counts = new HashMap<>();
        for (ItemStack item : items) {
            if (item != null) {
                counts.merge(item.getType(), item.getAmount(), Integer::sum);
            }
        }
        return counts;
    }

    private double getItemValue(Material material) {
        return switch (material) {
            case EMERALD -> 10.0;
            case DIAMOND -> 8.0;
            case GOLD_INGOT -> 4.0;
            case IRON_INGOT -> 2.0;
            default -> 1.0;
        };
    }

    // Additional event handlers for various trading activities
}
