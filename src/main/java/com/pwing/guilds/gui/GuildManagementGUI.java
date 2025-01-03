package com.pwing.guilds.gui;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.buffs.GuildBuff;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.meta.SkullMeta;
import java.util.*;

public class GuildManagementGUI {
    private final PwingGuilds plugin;

    public GuildManagementGUI(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, "Guild Management");

        // Members Management
        ItemStack members = createItem(Material.PLAYER_HEAD, "§6Members", "§7Click to manage guild members");
        inv.setItem(11, members);

        // Claims Management
        ItemStack claims = createItem(Material.MAP, "§6Claims Map", "§7View and manage claimed chunks");
        inv.setItem(13, claims);

        // Guild Settings
        ItemStack settings = createItem(Material.COMPARATOR, "§6Guild Settings", "§7Configure guild settings");
        inv.setItem(15, settings);

        player.openInventory(inv);
    }

    public void openClaimsMap(Player player, Guild guild) {
        Inventory inv = Bukkit.createInventory(null, 54, "Guild Claims Map");

        int centerX = player.getLocation().getChunk().getX();
        int centerZ = player.getLocation().getChunk().getZ();

        for (int z = -4; z <= 4; z++) {
            for (int x = -4; x <= 4; x++) {
                int slot = (z + 4) * 9 + (x + 4);
                int chunkX = centerX + x;
                int chunkZ = centerZ + z;

                ItemStack chunkItem;
                if (guild.isChunkClaimed(player.getWorld().getChunkAt(chunkX, chunkZ))) {
                    chunkItem = createItem(Material.EMERALD_BLOCK,
                            "§aClaimed Chunk",
                            "§7X: " + chunkX,
                            "§7Z: " + chunkZ,
                            "",
                            "§eClick to unclaim");
                } else {
                    chunkItem = createItem(Material.GRASS_BLOCK,
                            "§7Unclaimed Chunk",
                            "§7X: " + chunkX,
                            "§7Z: " + chunkZ,
                            "",
                            "§eClick to claim");
                }

                inv.setItem(slot, chunkItem);
            }
        }

        player.openInventory(inv);
    }

    public void openBuffsMenu(Player player, Guild guild) {
        Inventory inv = Bukkit.createInventory(null, 27, "Guild Buffs");

        Map<String, GuildBuff> buffs = plugin.getBuffManager().getAvailableBuffs();
        int slot = 10;

        for (Map.Entry<String, GuildBuff> entry : buffs.entrySet()) {
            GuildBuff buff = entry.getValue();

            List<String> lore = new ArrayList<>();
            lore.add("§7Level: §e" + buff.getLevel());
            lore.add("§7Cost: §e" + buff.getCost());
            lore.add("§7Duration: §e" + buff.getDuration() + "s");
            lore.add("");
            lore.add("§eClick to purchase!");

            ItemStack buffItem = createItem(
                    Material.POTION,
                    "§6" + buff.getName(),
                    lore.toArray(new String[0])
            );

            inv.setItem(slot++, buffItem);
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
}


