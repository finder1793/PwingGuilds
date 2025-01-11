package com.pwing.guilds.gui;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.buffs.GuildBuff;
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
import java.util.*;

/**
 * Handles all guild-related GUI menus.
 * Provides interfaces for guild management, member management, buffs, and territory claims.
 */
public class GuildManagementGUI {
    private final PwingGuilds plugin;

    /**
     * Creates a new GUI manager
     * @param plugin The plugin instance
     */
    public GuildManagementGUI(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Opens the main guild management menu for a player
     * @param player The player to show the menu to
     */
    public void openMainMenu(Player player) {
        Optional<Guild> playerGuild = plugin.getGuildManager().getPlayerGuild(player.getUniqueId());
        if (playerGuild.isEmpty()) {
            player.sendMessage("§cYou are not in a guild!");
            return;
        }

        GuildInventoryHolder holder = new GuildInventoryHolder(playerGuild.get());
        Inventory inv = Bukkit.createInventory(holder, 27, "Guild Management");
        // Members Management
        ItemStack members = createItem(Material.PLAYER_HEAD, "§6Members", "§7Click to manage guild members");
        inv.setItem(11, members);

        // Claims Management
        ItemStack claims = createItem(Material.MAP, "§6Claims Map", "§7View and manage claimed chunks");
        inv.setItem(13, claims);

        // Guild Settings
        ItemStack settings = createItem(Material.COMPARATOR, "§6Guild Settings", "§7Configure guild settings");
        inv.setItem(15, settings);

        // Add storage button
        ItemStack storage = createItem(Material.ENDER_CHEST, "§6Guild Storage", 
            "§7Access shared guild storage",
            "§7Store and retrieve items");
        inv.setItem(17, storage);

        player.openInventory(inv);
    }

    /**
     * Opens the guild territory claims map
     * @param player The player viewing the map
     * @param guild The guild whose claims to show
     */
    public void openClaimsMap(Player player, Guild guild) {
        Inventory inv = Bukkit.createInventory(null, 54, "Guild Claims Map");
        Chunk centerChunk = player.getLocation().getChunk();

        // Using full 9x6 chest dimensions
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

                // Calculate inventory slot for 9x6 layout
                int slot = (z + 3) * 9 + (x + 4);
                inv.setItem(slot, item);
            }
        }

        player.openInventory(inv);
    }

    /**
     * Opens the guild buffs management menu
     * @param player The player viewing the menu
     * @param guild The guild whose buffs to show
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
        meta.setDisplayName(name);
        meta.setLore(Arrays.asList(lore));
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Opens the guild member management interface
     * @param player The player viewing the menu
     * @param guild The guild to manage members for
     */
    public void openMemberManagement(Player player, Guild guild) {
        Inventory inv = Bukkit.createInventory(null, 54, "Guild Members");

        // Header items
        ItemStack info = new ItemStack(Material.BOOK);
        ItemMeta infoMeta = info.getItemMeta();
        infoMeta.setDisplayName("§6Guild Information");
        infoMeta.setLore(Arrays.asList(
                "§7Members: §f" + guild.getMembers().size() + "/" + guild.getPerks().getMemberLimit(),
                "§7Leader: §f" + Bukkit.getOfflinePlayer(guild.getLeader()).getName()
        ));
        info.setItemMeta(infoMeta);
        inv.setItem(4, info);

        // Member list
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

        // Navigation
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta backMeta = back.getItemMeta();
        backMeta.setDisplayName("§cBack to Main Menu");
        back.setItemMeta(backMeta);
        inv.setItem(49, back);

        player.openInventory(inv);
    }

    /**
     * Custom inventory holder for guild GUIs
     */
    public class GuildInventoryHolder implements InventoryHolder {
        private final Guild guild;

        public GuildInventoryHolder(Guild guild) {
            this.guild = guild;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }

        public Guild getGuild() {
            return guild;
        }
    }
}



