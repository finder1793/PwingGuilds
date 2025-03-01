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
        // Register this class as an event listener - we'll handle all GUI interactions in one place
        Bukkit.getPluginManager().registerEvents(this, plugin);
        
        // Remove the anonymous listener that was here - we'll consolidate the handling
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
        Inventory inv = Bukkit.createInventory(new GuildInventoryHolder(guild), 54, title);
        Chunk centerChunk = player.getLocation().getChunk();

        for (int x = -4; x <= 4; x++) {
            for (int z = -3; x <= 2; z++) {
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

        // Add back button
        ItemStack back = createItem(Material.ARROW, "§cBack to Main Menu");
        inv.setItem(49, back);

        player.openInventory(inv);
        // Removed anonymous listener - now handled in main onInventoryClick
    }

    /**
     * Opens the buffs menu for the player.
     * @param player The player to open the menu for
     * @param guild The guild to show buffs for
     */
    public void openBuffsMenu(Player player, Guild guild) {
        String title = plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.buffs", "Guild Buffs");
        Inventory inv = Bukkit.createInventory(new GuildInventoryHolder(guild), 54, title);

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
        
        // Add back button
        ItemStack back = createItem(Material.ARROW, "§cBack to Main Menu");
        inv.setItem(49, back);

        player.openInventory(inv);
        // Removed anonymous listener - now handled in main onInventoryClick
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
        Inventory inv = Bukkit.createInventory(new GuildInventoryHolder(guild), 54, title);

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
        // Removed anonymous listener - now handled in main onInventoryClick
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
    }

    /**
     * Opens the alliance management menu for the player.
     * @param player The player to open the menu for
     * @param guild The guild to manage alliances for
     */
    public void openAllianceManagement(Player player, Guild guild) {
        String title = plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.alliances", "Guild Alliances");
        Inventory inv = Bukkit.createInventory(new GuildInventoryHolder(guild), 27, title);

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
        // Removed anonymous listener - now handled in main onInventoryClick
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
     * Handles inventory click events for all guild GUIs.
     * @param event The inventory click event
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // First, prevent any item movement in our GUIs
        if (event.getView().getTitle().equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.main")) ||
            event.getView().getTitle().equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.settings")) ||
            event.getView().getTitle().equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.members")) ||
            event.getView().getTitle().equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.claims")) ||
            event.getView().getTitle().equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.alliances")) ||
            event.getView().getTitle().equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.buffs"))) {
            
            event.setCancelled(true);
        } else {
            // If not one of our GUIs, exit early
            return;
        }

        Player player = (Player) event.getWhoClicked();
        String title = event.getView().getTitle();
        
        // Only proceed if the click is in a GuildInventoryHolder
        if (!(event.getInventory().getHolder() instanceof GuildInventoryHolder)) {
            return;
        }
        
        Guild guild = ((GuildInventoryHolder) event.getInventory().getHolder()).getGuild();
        
        // Main Menu handling
        if (title.equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.main", "Guild Management"))) {
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
                default:
                    break;
            }
        }
        // Settings Menu handling
        else if (title.equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.settings", "Guild Settings"))) {
            switch (event.getSlot()) {
                case 11: // Change Guild Name
                    player.sendMessage("Enter the new guild name:");
                    plugin.getChatManager().expectResponse(player, response -> {
                        String newName = response.getMessage();
                        Bukkit.getScheduler().runTask(plugin, () -> {
                            if (guild.setName(newName)) {
                                player.sendMessage("Guild name changed successfully to " + newName);
                            } else {
                                player.sendMessage("Failed to change guild name.");
                            }
                        });
                    });
                    player.closeInventory();
                    break;
                case 13: // Set Guild Description
                    player.sendMessage("Enter the new guild description:");
                    plugin.getChatManager().expectResponse(player, response -> {
                        String newDescription = response.getMessage();
                        guild.setDescription(newDescription);
                        player.sendMessage("Guild description set successfully.");
                    });
                    player.closeInventory();
                    break;
                case 15: // Toggle PvP
                    guild.setPvPEnabled(!guild.isPvPEnabled());
                    player.sendMessage("PvP is now " + (guild.isPvPEnabled() ? "enabled" : "disabled") + " in guild territories.");
                    // Refresh the menu to show the updated status
                    openGuildSettings(player, guild);
                    break;
                case 26: // Back to Main Menu
                    openMainMenu(player);
                    break;
                default:
                    break;
            }
        }
        // Members Menu handling
        else if (title.equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.members", "Guild Members"))) {
            if (event.getSlot() == 49) { // Back button
                openMainMenu(player);
            } else if (event.getSlot() >= 9 && event.getSlot() < 9 + guild.getMembers().size()) {
                // Handle member click - get the member based on position
                int memberIndex = event.getSlot() - 9;
                List<UUID> members = new ArrayList<>(guild.getMembers());
                if (memberIndex < members.size()) {
                    UUID memberId = members.get(memberIndex);
                    // Show member management options (kick, promote, etc.)
                    handleMemberClick(player, guild, memberId);
                }
            }
        }
        // Claims Map handling
        else if (title.equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.claims"))) {
            if (event.getSlot() == 49) { // Back button
                openMainMenu(player);
            } else {
                // Handle claim/unclaim logic
                Chunk centerChunk = player.getLocation().getChunk();
                int slot = event.getSlot();
                int row = slot / 9;
                int col = slot % 9;
                
                int chunkX = centerChunk.getX() + (col - 4);
                int chunkZ = centerChunk.getZ() + (row - 3);
                
                Chunk clickedChunk = player.getWorld().getChunkAt(chunkX, chunkZ);
                
                // Convert Bukkit Chunk to ChunkLocation that Guild methods expect
                com.pwing.guilds.guild.ChunkLocation chunkLocation = new com.pwing.guilds.guild.ChunkLocation(
                    clickedChunk.getWorld().getName(), clickedChunk.getX(), clickedChunk.getZ());
                
                if (guild.isChunkClaimed(clickedChunk)) {
                    // Unclaim the chunk
                    if (guild.unclaimChunk(chunkLocation)) {
                        player.sendMessage("Chunk unclaimed successfully!");
                    } else {
                        player.sendMessage("Failed to unclaim chunk.");
                    }
                } else {
                    // Claim the chunk
                    if (guild.claimChunk(chunkLocation)) {
                        player.sendMessage("Chunk claimed successfully!");
                    } else {
                        player.sendMessage("Failed to claim chunk. Check if you have enough claim slots.");
                    }
                }
                
                // Refresh the claims map
                openClaimsMap(player, guild);
            }
        }
        // Alliance Management handling
        else if (title.equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.alliances", "Guild Alliances"))) {
            switch (event.getSlot()) {
                case 11: // Create Alliance
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
                    player.closeInventory();
                    break;
                case 13: // Join Alliance
                    player.sendMessage("Enter the name of the alliance to join:");
                    plugin.getChatManager().expectResponse(player, response -> {
                        String allianceName = response.getMessage();
                        if (plugin.getAllianceManager().addGuildToAlliance(allianceName, guild)) {
                            player.sendMessage("Joined alliance successfully.");
                        } else {
                            player.sendMessage("Failed to join alliance. Make sure the alliance exists and you are not already a member.");
                        }
                    });
                    player.closeInventory();
                    break;
                case 15: // Leave Alliance
                    if (guild.getAlliance() != null) {
                        plugin.getAllianceManager().removeGuildFromAlliance(guild.getAlliance().getName(), guild);
                        player.sendMessage("Left alliance successfully.");
                    } else {
                        player.sendMessage("You are not part of any alliance.");
                    }
                    break;
                case 26: // Back to Main Menu
                    openMainMenu(player);
                    break;
                default:
                    break;
            }
        }
        // Buffs Menu handling
        else if (title.equals(plugin.getConfigManager().getConfig("gui.yml").getString("gui.titles.buffs"))) {
            if (event.getSlot() == 49) { // Back button
                openMainMenu(player);
            } else {
                Map<String, GuildBuff> buffs = plugin.getBuffManager().getAvailableBuffs();
                for (Map.Entry<String, GuildBuff> entry : buffs.entrySet()) {
                    String buffId = entry.getKey();
                    GuildBuff buff = entry.getValue();
                    if (buff.getSlot() == event.getSlot()) {
                        // Use the correct method name from GuildBuffManager
                        if (plugin.getBuffManager().activateGuildBuff(guild, buffId)) {
                            player.sendMessage("Activated buff: " + buff.getName());
                        } else {
                            player.sendMessage("Failed to activate buff. Do you have enough resources?");
                        }
                        break;
                    }
                }
            }
        }
    }

    /**
     * Handles actions when a member is clicked in the member management GUI.
     * @param player The player clicking
     * @param guild The guild
     * @param memberId The UUID of the member being managed
     */
    private void handleMemberClick(Player player, Guild guild, UUID memberId) {
        // This would typically open a new menu with options for the member
        String memberName = Bukkit.getOfflinePlayer(memberId).getName();
        player.sendMessage("Managing member: " + memberName);
        
        // Here you could implement a new menu with options like kick, promote, etc.
        // For now, just provide some basic functionality
        if (guild.getLeader().equals(player.getUniqueId())) {
            if (!memberId.equals(guild.getLeader())) {
                player.sendMessage("You can kick this member by typing /guild kick " + memberName);
            }
        } else {
            player.sendMessage("Only the guild leader can manage members.");
        }
    }
}



