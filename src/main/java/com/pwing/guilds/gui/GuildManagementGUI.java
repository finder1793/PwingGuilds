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
        String title = plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.main", "Guild Management");
        Inventory inv = Bukkit.createInventory(holder, 27, title);

        // Members Management
        ItemBuilder members = ItemBuilder.fromConfig("members", plugin, player);
        inv.setItem(11, members.build());

        // Claims Management
        ItemBuilder claims = ItemBuilder.fromConfig("claims", plugin, player);
        inv.setItem(13, claims.build());

        // Guild Settings
        ItemBuilder settings = ItemBuilder.fromConfig("settings", plugin, player);
        inv.setItem(15, settings.build());

        // Storage
        ItemBuilder storage = ItemBuilder.fromConfig("storage", plugin, player);
        inv.setItem(17, storage.build());

        player.openInventory(inv);
    }

    /**
     * Opens the claims map for the player.
     * @param player The player to open the map for
     * @param guild The guild to show claims for
     */
    public void openClaimsMap(Player player, Guild guild) {
        String title = plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.claims", "Guild Claims Map");
        Inventory inv = Bukkit.createInventory(null, 54, title);
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

        // Add event handler to prevent items from being taken out of the claims map GUI
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onClaimsMapClick(InventoryClickEvent event) {
                if (!event.getView().getTitle().equals(title)) {
                    return;
                }

                event.setCancelled(true); // Prevent items from being taken out of the claims map GUI
                // Handle claims map specific logic here if needed
            }
        }, plugin);
    }

    /**
     * Opens the buffs menu for the player.
     * @param player The player to open the menu for
     * @param guild The guild to show buffs for
     */
    public void openBuffsMenu(Player player, Guild guild) {
        String title = plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.buffs", "Guild Buffs");
        Inventory inv = Bukkit.createInventory(null, 54, title);

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

        // Add event handler to prevent items from being taken out of the buffs menu GUI
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onBuffsMenuClick(InventoryClickEvent event) {
                if (!event.getView().getTitle().equals(title)) {
                    return;
                }

                event.setCancelled(true); // Prevent items from being taken out of the buffs menu GUI
                // Handle buffs menu specific logic here if needed
            }
        }, plugin);
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
        String title = plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.members", "Guild Members");
        Inventory inv = Bukkit.createInventory(null, 54, title);

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

        // Add event handler to prevent items from being taken out of the member management GUI
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onMemberManagementClick(InventoryClickEvent event) {
                if (!event.getView().getTitle().equals(title)) {
                    return;
                }

                event.setCancelled(true); // Prevent items from being taken out of the member management GUI
                // Handle member management specific logic here if needed
            }
        }, plugin);
    }

    /**
     * Opens the guild settings menu for the player.
     * @param player The player to open the menu for
     * @param guild The guild to manage settings for
     */
    public void openGuildSettings(Player player, Guild guild) {
        String title = plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.settings", "Guild Settings");
        GuildInventoryHolder holder = new GuildInventoryHolder(guild);
        Inventory inv = Bukkit.createInventory(holder, 27, title);

        // Change Guild Name
        ItemStack changeName = createItem(Material.NAME_TAG, "§6Change Guild Name", "§7Click to change the guild name");
        inv.setItem(11, changeName);

        // Set Guild Description
        ItemStack setDescription = createItem(Material.BOOK, "§6Set Guild Description", "§7Click to set the guild description");
        inv.setItem(13, setDescription);

        // Toggle PvP
        ItemStack togglePvP = createItem(Material.IRON_SWORD, "§6Toggle PvP", "§7Click to toggle PvP within the guild");
        inv.setItem(15, togglePvP);

        // Back to Main Menu
        ItemStack back = createItem(Material.ARROW, "§cBack to Main Menu");
        inv.setItem(26, back);

        player.openInventory(inv);

        // Add event handler to prevent items from being taken out of the guild settings GUI
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onGuildSettingsClick(InventoryClickEvent event) {
                if (!event.getView().getTitle().equals(title)) {
                    return;
                }

                event.setCancelled(true); // Prevent items from being taken out of the guild settings GUI
                Player player = (Player) event.getWhoClicked();
                Guild guild = ((GuildInventoryHolder) event.getInventory().getHolder()).getGuild();
                switch (event.getSlot()) {
                    case 11:
                        player.sendMessage("Enter the new guild name:");
                        plugin.getChatManager().expectResponse(player, response -> {
                            String newName = response.getMessage();
                            if (guild.setName(newName)) {
                                player.sendMessage("Guild name changed successfully to " + newName);
                            } else {
                                player.sendMessage("Failed to change guild name.");
                            }
                        });
                        break;
                    case 13:
                        player.sendMessage("Enter the new guild description:");
                        plugin.getChatManager().expectResponse(player, response -> {
                            String newDescription = response.getMessage();
                            guild.setDescription(newDescription);
                            player.sendMessage("Guild description set successfully.");
                        });
                        break;
                    case 15:
                        guild.setPvPEnabled(!guild.isPvPEnabled());
                        player.sendMessage("PvP is now " + (guild.isPvPEnabled() ? "enabled" : "disabled") + " in guild territories.");
                        break;
                    case 26:
                        openMainMenu(player);
                        break;
                    default:
                        break;
                }
            }
        }, plugin);
    }

    /**
     * Opens the alliance management menu for the player.
     * @param player The player to open the menu for
     * @param guild The guild to manage alliances for
     */
    public void openAllianceManagement(Player player, Guild guild) {
        String title = plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.alliances", "Guild Alliances");
        Inventory inv = Bukkit.createInventory(null, 27, title);

        // Create Alliance
        ItemStack createAlliance = createItem(Material.PAPER, "§6Create Alliance", "§7Click to create a new alliance");
        inv.setItem(11, createAlliance);

        // Join Alliance
        ItemStack joinAlliance = createItem(Material.BOOK, "§6Join Alliance", "§7Click to join an existing alliance");
        inv.setItem(13, joinAlliance);

        // Leave Alliance
        ItemStack leaveAlliance = createItem(Material.BARRIER, "§6Leave Alliance", "§7Click to leave the current alliance");
        inv.setItem(15, leaveAlliance);

        // Back to Main Menu
        ItemStack back = createItem(Material.ARROW, "§cBack to Main Menu");
        inv.setItem(26, back);

        player.openInventory(inv);

        // Add event handler to prevent items from being taken out of the alliance management GUI
        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onAllianceManagementClick(InventoryClickEvent event) {
                if (!event.getView().getTitle().equals(title)) {
                    return;
                }

                event.setCancelled(true); // Prevent items from being taken out of the alliance management GUI
                Player player = (Player) event.getWhoClicked();
                switch (event.getSlot()) {
                    case 11:
                        // Implement create alliance logic
                        player.sendMessage("Enter the name of the new alliance:");
                        plugin.getChatManager().expectResponse(player, response -> {
                            String allianceName = response.getMessage();
                            if (plugin.getAllianceManager().getAlliance(allianceName).isPresent()) {
                                player.sendMessage("An alliance with that name already exists.");
                            } else {
                                plugin.getAllianceManager().createAlliance(allianceName, guild);
                                player.sendMessage("Alliance created successfully.");
                            }
                        });
                        break;
                    case 13:
                        // Implement join alliance logic
                        player.sendMessage("Enter the name of the alliance to join:");
                        plugin.getChatManager().expectResponse(player, response -> {
                            String allianceName = response.getMessage();
                            if (plugin.getAllianceManager().addGuildToAlliance(allianceName, guild)) {
                                player.sendMessage("Joined alliance successfully.");
                            } else {
                                player.sendMessage("Failed to join alliance. Make sure the alliance exists and you are not already a member.");
                            }
                        });
                        break;
                    case 15:
                        // Implement leave alliance logic
                        if (guild.getAlliance() != null) {
                            plugin.getAllianceManager().removeGuildFromAlliance(guild.getAlliance().getName(), guild);
                            player.sendMessage("Left alliance successfully.");
                        } else {
                            player.sendMessage("You are not part of any alliance.");
                        }
                        break;
                    case 26:
                        openMainMenu(player);
                        break;
                    default:
                        break;
                }
            }
        }, plugin);
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

        event.setCancelled(true); // Prevent items from being taken out of the GUI
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
                openGuildSettings(player, guild);
                break;
            case 17:
                plugin.getStorageManager().openStorage(player, guild);
                break;
            case 19:
                openAllianceManagement(player, guild);
                break;
            case 49:
                openMainMenu(player);
                break;
            default:
                break;
        }
    }
}



