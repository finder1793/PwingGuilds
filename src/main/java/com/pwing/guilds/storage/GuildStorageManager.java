package com.pwing.guilds.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildManager;

import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.EventPriority;

import java.util.Date;
import java.text.SimpleDateFormat;
import java.util.Set;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildStorageManager implements Listener {
    private final PwingGuilds plugin;
    private final GuildManager guildManager;
    private final Map<UUID, Inventory> openStorages = new HashMap<>();
    private final Map<String, ItemStack[]> guildStorages = new HashMap<>();

    public GuildStorageManager(PwingGuilds plugin) {
        this.plugin = plugin;
        this.guildManager = plugin.getGuildManager();
        Bukkit.getPluginManager().registerEvents(this, plugin);
        loadAllStorages();
        
        // Add shutdown hook
        Bukkit.getPluginManager().registerEvent(PluginDisableEvent.class, this, EventPriority.HIGHEST,
            (listener, event) -> closeAllStorages(), plugin);
    }

    public void openStorage(Player player, Guild guild) {
        if (!guild.getPerks().activatePerk("guild-storage")) {
            return;
        }

        int rows = guild.getPerks().getStorageRows();
        Inventory inv = Bukkit.createInventory(null, rows * 9, "Guild Storage - " + guild.getName());
        
        ItemStack[] contents = guildStorages.get(guild.getName());
        if (contents != null) {
            inv.setContents(contents);
        }

        player.openInventory(inv);
        openStorages.put(player.getUniqueId(), inv);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Inventory openInv = openStorages.get(player.getUniqueId());
        if (openInv != null && event.getInventory().equals(openInv)) {
            // Additional permission checks could go here
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player player)) {
            return;
        }

        Inventory openInv = openStorages.remove(player.getUniqueId());
        if (openInv != null) {
            String guildName = event.getView().getTitle().substring("Guild Storage - ".length());
            guildStorages.put(guildName, openInv.getContents());
            saveStorage(guildName);
        }
    }

    private void loadAllStorages() {
        for (Guild guild : plugin.getGuildManager().getGuilds()) {
            ConfigurationSection section = guildManager.getStorage().getStorageData(guild.getName());
            if (section != null) {
                ItemStack[] contents = (ItemStack[]) section.get("contents");
                guildStorages.put(guild.getName(), contents);
            }
        }
    }

    private void saveStorageAsync(String guildName) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            ItemStack[] contents = guildStorages.get(guildName);
            if (contents != null) {
                guildManager.getStorage().saveStorageData(guildName, contents);
            }
        });
    }

    public void saveAllStorages() {
        guildStorages.forEach((guildName, contents) -> 
            guildManager.getStorage().saveStorageData(guildName, contents));
    }

    private final Map<String, Set<UUID>> activeViewers = new HashMap<>();

    public Set<UUID> getActiveViewers(String guildName) {
        return activeViewers.computeIfAbsent(guildName, k -> new HashSet<>());
    }

    private boolean hasStorageAccess(Player player, Guild guild) {
        return guild.getMembers().contains(player.getUniqueId()) && 
               player.hasPermission("guilds.storage.access");
    }

    private boolean validateStorageSize(Guild guild, ItemStack[] contents) {
        int maxSize = guild.getPerks().getStorageRows() * 9;
        return contents.length <= maxSize;
    }

    private void logStorageTransaction(Player player, Guild guild, String action) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
        plugin.getLogger().info(String.format("[Storage] %s: %s performed %s in guild %s", 
            timestamp, player.getName(), action, guild.getName()));
    }

    private void closeAllStorages() {
        if (plugin.getGuildManager() != null && plugin.getGuildManager().getStorage() != null) {
            plugin.getLogger().info("Starting final guild data save...");
            for (Guild guild : plugin.getGuildManager().getGuilds()) {
                plugin.getGuildManager().getStorage().saveGuild(guild);
            }
            plugin.getLogger().info("Guild data save completed!");
        }
    }

    private void saveStorage(String guildName) {
        ItemStack[] contents = guildStorages.get(guildName);
        if (contents != null) {
            plugin.getGuildManager().getStorage().saveStorageData(guildName, contents);
        }
    }


    public ItemStack[] getGuildStorage(String guildName) {
        return guildStorages.get(guildName);
    }

    public void saveInventory(Inventory inventory) {
        if (inventory == null || !inventory.getViewers().get(0).getOpenInventory().getTitle().contains("Guild Storage")) {
            return;
        }
        
        String title = inventory.getViewers().get(0).getOpenInventory().getTitle();
        String guildName = title.substring(title.indexOf(":") + 2);
        
        plugin.getGuildManager().getStorage().saveStorageData(guildName, inventory.getContents());
    }
}