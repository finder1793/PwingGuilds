package com.pwing.guilds.gui;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.ChunkLocation;
import com.pwing.guilds.buffs.GuildBuff;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.OfflinePlayer;
import org.bukkit.potion.PotionEffect;

import java.util.UUID;

public final class GuildGUIListener implements Listener {
    private final PwingGuilds plugin;

    public GuildGUIListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String title = event.getView().getTitle();
        if (!title.equals("Guild Management") &&
            !title.equals("Guild Claims Map") &&
            !title.equals("Guild Buffs")) {
            return;
        }

        event.setCancelled(true);

        if (title.equals("Guild Claims Map")) {
            handleClaimsMapClick(event, player);
        } else if (title.equals("Guild Buffs")) {
            handleBuffsMenuClick(event, player);
        } else {
            handleMainMenuClick(event, player);
        }
    }

    private void handleClaimsMapClick(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) return;

        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            var meta = event.getCurrentItem().getItemMeta();
            var lore = meta.getLore();
            if (lore == null || lore.size() < 2) return;

            int x = Integer.parseInt(lore.get(0).substring(4));
            int z = Integer.parseInt(lore.get(1).substring(4));

            if (event.getCurrentItem().getType() == Material.EMERALD_BLOCK) {
                guild.unclaimChunk(new ChunkLocation(player.getWorld().getChunkAt(x, z)));
                player.sendMessage("§aChunk unclaimed!");
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
            } else {
                if (guild.canClaim()) {
                    guild.claimChunk(new ChunkLocation(player.getWorld().getChunkAt(x, z)));
                    player.sendMessage("§aChunk claimed!");
                    player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                } else {
                    player.sendMessage("§cYou've reached your maximum claim limit!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                }
            }

            new GuildManagementGUI(plugin).openClaimsMap(player, guild);
        });
    }

    private void handleBuffsMenuClick(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) return;

        plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
            var meta = event.getCurrentItem().getItemMeta();
            String buffName = meta.getDisplayName();
            GuildBuff buff = plugin.getBuffManager().getAvailableBuffs().values().stream()
                    .filter(b -> b.getName().equals(buffName))
                    .findFirst()
                    .orElse(null);

            if (buff != null) {
                if (!player.hasPermission(buff.getPermission())) {
                    player.sendMessage("§cYou don't have permission to purchase this buff!");
                    player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
                    return;
                }

                for (UUID member : guild.getMembers()) {
                    Player guildMember = Bukkit.getPlayer(member);
                    if (guildMember != null && guildMember.isOnline()) {
                        guildMember.addPotionEffect(new PotionEffect(
                                buff.getEffectType(),
                                buff.getDuration() * 20,
                                buff.getLevel() - 1
                        ));
                        guildMember.playSound(guildMember.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
                    }
                }

                player.sendMessage("§aSuccessfully purchased " + buff.getName() + " for your guild!");
            }
        });
    }

    private void handleMainMenuClick(InventoryClickEvent event, Player player) {
        if (event.getCurrentItem() == null) return;

        switch (event.getSlot()) {
            case 11 -> {
                plugin.getGuildManager().getPlayerGuild(player.getUniqueId()).ifPresent(guild -> {
                    Inventory memberInv = Bukkit.createInventory(null, 54, "Guild Members");
                    int slot = 0;
                    for (UUID memberId : guild.getMembers()) {
                        OfflinePlayer member = Bukkit.getOfflinePlayer(memberId);
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
                        SkullMeta skullMeta = (SkullMeta) head.getItemMeta();
                        skullMeta.setOwningPlayer(member);
                        skullMeta.setDisplayName("§e" + member.getName());
                        head.setItemMeta(skullMeta);
                        memberInv.setItem(slot++, head);
                    }

                    ItemStack back = new ItemStack(Material.ARROW);
                    var backMeta = back.getItemMeta();
                    backMeta.setDisplayName("§cBack to Main Menu");
                    back.setItemMeta(backMeta);
                    memberInv.setItem(49, back);

                    player.openInventory(memberInv);
                });
            }
            case 13 -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .ifPresent(guild -> new GuildManagementGUI(plugin).openClaimsMap(player, guild));
            case 15 -> plugin.getGuildManager().getPlayerGuild(player.getUniqueId())
                    .ifPresent(guild -> new GuildManagementGUI(plugin).openBuffsMenu(player, guild));
            case 26 -> player.closeInventory();
        }
    }
}