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
import org.bukkit.inventory.meta.SkullMeta;
import java.util.*;

public class GuildManagementGUI {
    private final PwingGuilds plugin;

    public GuildManagementGUI(PwingGuilds plugin) {
        this.plugin = plugin;
    }

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

    public void openClaimsMap(Player player, Guild guild) {
        // Change inventory size to 54 (6 rows)
        Inventory inv = Bukkit.createInventory(null, 54, "Guild Claims Map");

        int centerX = player.getLocation().getChunk().getX();
        int centerZ = player.getLocation().getChunk().getZ();

        // Adjust the loop to ensure we don't exceed inventory bounds
        for (int z = -3; z <= 3; z++) {
            for (int x = -3; x <= 3; x++) {
                int slot = (z + 3) * 9 + (x + 3);
                if (slot >= 0 && slot < 54) {  // Add bounds check
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
        }

        player.openInventory(inv);
    }

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

