package com.pwing.guilds.gui;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.buffs.GuildBuff;
import com.pwing.guilds.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.OfflinePlayer;
import org.bukkit.Chunk;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.ChatColor;
import java.util.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

/**
 * Manages the GUI for guild management.
 */
public class GuildManagementGUI implements Listener {
    private final PwingGuilds plugin;

    /**
     * Constructs a new GuildManagementGUI instance.
     * @param plugin The main plugin instance
     */
    public GuildManagementGUI(PwingGuilds plugin) {
        this.plugin = plugin;
        // Register this class as an event listener
        Bukkit.getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Opens the main menu for the player.
     * @param player The player to open the menu for
     */
    public void openMainMenu(Player player) {
        Optional<Guild> playerGuild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (playerGuild.isEmpty()) {
            player.sendMessage(plugin.getMessageManager().getMessage("general.not-in-guild"));
            return;
        }

        GuildInventoryHolder holder = new GuildInventoryHolder(playerGuild.get());
        String title = plugin.getMessageManager().getMessage("gui.titles.main");
        Inventory inv = Bukkit.createInventory(holder, 27, title);

        // Members Management
        ItemBuilder members = ItemBuilder.fromConfig("members", plugin, player)
            .name(plugin.getMessageManager().getMessage("gui.items.members.name"))
            .lore(plugin.getMessageManager().getMessage("gui.items.members.lore"));
        inv.setItem(11, members.build());

        // Claims Management
        ItemBuilder claims = ItemBuilder.fromConfig("claims", plugin, player)
            .name(plugin.getMessageManager().getMessage("gui.items.claims.name"))
            .lore(plugin.getMessageManager().getMessage("gui.items.claims.lore"));
        inv.setItem(13, claims.build());

        // Guild Settings
        ItemBuilder settings = ItemBuilder.fromConfig("settings", plugin, player)
            .name(plugin.getMessageManager().getMessage("gui.items.settings.name"))
            .lore(plugin.getMessageManager().getMessage("gui.items.settings.lore"));
        inv.setItem(15, settings.build());

        // Storage
        ItemBuilder storage = ItemBuilder.fromConfig("storage", plugin, player)
            .name(plugin.getMessageManager().getMessage("gui.items.storage.name"))
            .lore(plugin.getMessageManager().getMessage("gui.items.storage.lore"));
        inv.setItem(17, storage.build());

        player.openInventory(inv);
    }

    /**
     * Opens the claims map for the player.
     * @param player The player to open the map for
     * @param guild The guild to show claims for
     */
    public void openClaimsMap(Player player, Guild guild) {
        Inventory inv = Bukkit.createInventory(null, 54, "Guild Claims Map");
        Chunk centerChunk = player.getLocation().getChunk();

        for (int x = -4; x <= 4; x++) {
            for (int z = -3; z <= 2; z++) {
                Chunk chunk = player.getWorld().getChunkAt(centerChunk.getX() + x, centerChunk.getZ() + z);
                ItemStack item;

                if (guild.isChunkClaimed(chunk)) {
                    item = new ItemStack(Material.EMERALD_BLOCK);
                    var meta = item.getItemMeta();
                    meta.setDisplayName("§aClaimed Chunk");
                    meta.setLore(Arrays.asList(
                        "§7X: " + chunk.getX(),
                        "§7Z: " + chunk.getZ(),
                        "",
                        "§eClick to unclaim"
                    ));
                    item.setItemMeta(meta);
                } else {
                    item = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
                    var meta = item.getItemMeta();
                    meta.setDisplayName("§7Unclaimed Chunk");
                    meta.setLore(Arrays.asList(
                        "§7X: " + chunk.getX(),
                        "§7Z: " + chunk.getZ(),
                        "",
                        "§eClick to claim"
                    ));
                    item.setItemMeta(meta);
                }

                int slot = (z + 3) * 9 + (x + 4);
                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Opens the buffs menu for the player.
     * @param player The player to open the menu for
     * @param guild The guild to show buffs for
     */
    public void openBuffsMenu(Player player, Guild guild) {
        Inventory inv = Bukkit.createInventory(null, 54, "Guild Buffs");

        Map<String, GuildBuff> buffs = plugin.getBuffManager().getAvailableBuffs();
        for (Map.Entry<String, GuildBuff> entry : buffs.entrySet()) {
            GuildBuff buff = entry.getValue();

            ItemStack buffItem = new ItemStack(buff.getMaterial());
            ItemMeta meta = buffItem.getItemMeta();
            meta.setDisplayName("§6" + buff.getName());

            List<String> lore = new ArrayList<>();
            lore.add("§7Level: §e" + buff.getLevel());
            lore.add("§7Cost: §e" + buff.getCost());
            lore.add("§7Duration: §e" + buff.getDuration() + "s");
            lore.add("");
            lore.add("§eClick to activate!");

            meta.setLore(lore);
            buffItem.setItemMeta(meta);

            inv.setItem(buff.getSlot(), buffItem);
        }

        player.openInventory(inv);
    }

    private ItemStack createItem(Material material, String name, String... lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', name));
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Opens the member management menu for the player.
     * @param player The player to open the menu for
     * @param guild The guild to manage members for
     */
    public void openMemberManagement(Player player, Guild guild) {
        Inventory inv = Bukkit.createInventory(null, 54, "Guild Members");

        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6Guild Information");
        infoMeta.setLore(Arrays.asList(
                "§7Members: §f" + guild.getMembers().size() + "/" + guild.getPerks().getMemberLimit(),
                "§7Leader: §f" + Bukkit.getOfflinePlayer(guild.getLeader()).getName()
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        int slot = 9;
        for (UUID memberId : guild.getMembers()) {
            OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
            ItemStack memberHead = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) memberHead.getItemMeta();
            skullMeta.setDisplayName("§e" + member.getName());
            skullMeta.setLore(Arrays.asList(
                    "§7Click to manage this member",
                    memberId.equals(guild.getLeader()) ? "§6Guild Leader" : "§7Guild Member",
                    "§7Last Seen: §f" + (member.isOnline() ? "Online" : "Offline")
            ));
            memberHead.setItemMeta(skullMeta);
            inv.setItem(slot++, memberHead);
        }

        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cBack to Main Menu");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    /**
     * Holds the inventory for the guild management GUI.
     */
    public class GuildInventoryHolder implements InventoryHolder {
        private final Guild guild;

        /**
         * Constructs a new GuildInventoryHolder instance.
         * @param guild The guild associated with the inventory
         */
        public GuildInventoryHolder(Guild guild) {
            this.guild = guild;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        /**
         * Gets the guild associated with the inventory.
         * @return The guild
         */
        public Guild getGuild() {
            return guild;
        }
    }

    /**
     * Handles inventory click events for the guild management GUI.
     * @param event The inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getInventory().getHolder() instanceof GuildInventoryHolder)) {
            return;
        }

        event.setCancelled(true);
        Player player = (Player) event.getWhoClicked();
        GuildInventoryHolder holder = (GuildInventoryHolder) event.getInventory().getHolder();
        Guild guild = holder.getGuild();

        switch (event.getSlot()) {
            case 11:
                openMemberManagement(player, guild);
                break;
            case 13:
                openClaimsMap(player, guild);
                break;
            case 15:
                // Open guild settings (implement this method)
                break;
            case 17:
                // Open guild storage (implement this method)
                break;
            case 49:
                openMainMenu(player);
                break;
            default:
                break;
        }
    }
}



