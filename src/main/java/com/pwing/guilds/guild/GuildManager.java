package com.pwing.guilds.guild;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.Collection;

import com.pwing.guilds.storage.GuildStorage;
import com.pwing.guilds.integration.WorldGuardHook;
import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.api.GuildClaimChunkEvent;
import com.pwing.guilds.api.GuildCreateEvent;
import com.pwing.guilds.api.GuildDeleteEvent;
import com.pwing.guilds.api.GuildMemberLeaveEvent;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.WorldEditException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;

/**
 * Manages all guild-related operations and data within the plugin.
 * Handles creating, deleting, and managing guilds as well as their storage.
 */
public class GuildManager {
    private final PwingGuilds plugin;
    private final Map<String, Guild> guilds;
    private final Map<UUID, Guild> playerGuilds = new HashMap<>();
    private final Map<ChunkLocation, Guild> claimedChunks = new HashMap<>();
    private final GuildStorage storage;
    private final WorldGuardHook worldGuardHook;

    /**
     * Creates a new GuildManager instance
     * @param plugin Main plugin instance
     * @param storage Storage implementation to use
     * @param worldGuardHook WorldGuard integration hook
     */
    public GuildManager(PwingGuilds plugin, GuildStorage storage, WorldGuardHook worldGuardHook) {
        this.plugin = plugin;
        this.storage = storage;
        this.guilds = new HashMap<>();
        this.worldGuardHook = worldGuardHook;
    }

    /**
     * Gets the map of player UUIDs to their guild memberships
     * @return Map of player UUIDs to guilds
     */
    public Map<UUID, Guild> getPlayerGuilds() {
        return playerGuilds;
    }

    /**
     * Initializes the guild manager and loads all guilds from storage
     */
    public void initialize() {
        Set<Guild> loadedGuilds = storage.loadAllGuilds();
        loadedGuilds.forEach(guild -> {
            guilds.put(guild.getName(), guild);
            guild.getMembers().forEach(member -> playerGuilds.put(member, guild));
            guild.getClaimedChunks().forEach(chunk -> claimedChunks.put(chunk, guild));
        });
        plugin.getLogger().info("Loaded " + guilds.size() + " guilds with " + playerGuilds.size() + " members");
    }

    /**
     * Creates a new guild with the specified name and owner
     * @param name The name for the new guild
     * @param owner UUID of the guild owner
     * @return true if guild was created successfully, false if owner already has a guild or name is taken
     */
    public boolean createGuild(String name, UUID owner) {
        if (playerGuilds.containsKey(owner)) {
            return false;
        }

        if (guilds.containsKey(name)) {
            return false;
        }

        Guild guild = new Guild(plugin, name, owner);
        guilds.put(name, guild);
        playerGuilds.put(owner, guild);
        storage.saveGuild(guild);
        Bukkit.getPluginManager().callEvent(new GuildCreateEvent(guild));
        return true;
    }

    /**
     * Adds a guild to the manager
     * @param guild Guild to add
     */
    public void addGuild(Guild guild) {
        guilds.put(guild.getName(), guild);
        guild.getMembers().forEach(member -> playerGuilds.put(member, guild));
        guild.getClaimedChunks().forEach(chunk -> claimedChunks.put(chunk, guild));
        storage.saveGuild(guild);
    }

    public void deleteGuild(String name) {
        Guild guild = guilds.remove(name);
        if (guild != null) {
            Bukkit.getPluginManager().callEvent(new GuildDeleteEvent(guild));
            guild.getMembers().forEach(playerGuilds::remove);
            guild.getClaimedChunks().forEach(claimedChunks::remove);
            storage.deleteGuild(name);
        }
    }

    /**
     * Claims a chunk for a guild if possible
     * @param guild The guild attempting to claim
     * @param chunk The chunk to claim
     * @return true if claim was successful, false if chunk is already claimed or guild cannot claim more
     */
    public boolean claimChunk(Guild guild, Chunk chunk) {
        if (guild == null || chunk == null) {
            return false;
        }
    
        ChunkLocation location = new ChunkLocation(chunk);
    
        // Validate if chunk is already claimed
        if (claimedChunks.containsKey(location)) {
            return false;
        }
    
        // Validate if guild can claim more chunks
        if (!guild.canClaim()) {
            return false;
        }
    
        // Validate WorldGuard rules if applicable
        if (plugin.hasWorldGuard() && !plugin.getWorldGuardHook().canClaim(chunk)) {
            return false;
        }
    
        GuildClaimChunkEvent event = new GuildClaimChunkEvent(guild, location);
        Bukkit.getPluginManager().callEvent(event);
        if (event.isCancelled()) {
            return false;
        }

        claimedChunks.put(location, guild);
        guild.claimChunk(location);
        storage.saveGuild(guild);
        return true;
    }

    public boolean unclaimChunk(Guild guild, Chunk chunk) {
        ChunkLocation location = new ChunkLocation(chunk);
        if (claimedChunks.get(location) == guild) {
            claimedChunks.remove(location);
            boolean success = guild.unclaimChunk(location);
            if (success) {
                storage.saveGuild(guild);
            }
            return success;
        }
        return false;
    }

    /**
     * Checks if a player can interact with blocks in a chunk
     * @param player UUID of player attempting interaction
     * @param chunk The chunk being interacted with
     * @return true if player can interact, false otherwise
     */
    public boolean canInteract(UUID player, Chunk chunk) {
        ChunkLocation location = new ChunkLocation(chunk);
        Guild guild = claimedChunks.get(location);
        return guild == null || guild.isMember(player);
    }

    /**
     * Gets a guild based on a claimed chunk
     * @param chunk The chunk to check
     * @return Optional containing the owning guild if found
     */
    public Optional<Guild> getGuildByChunk(Chunk chunk) {
        return Optional.ofNullable(claimedChunks.get(new ChunkLocation(chunk)));
    }

    /**
     * Gets the guild a player belongs to
     * @param player UUID of the player
     * @return Optional containing the player's guild if they are in one
     */
    public Optional<Guild> getPlayerGuild(UUID player) {
        return Optional.ofNullable(playerGuilds.get(player));
    }

    public boolean invitePlayer(String guildName, UUID inviter, UUID invited) {
        Guild guild = guilds.get(guildName);
        if (guild != null && guild.isMember(inviter)) {
            boolean success = guild.invite(invited);
            if (success) {
                storage.saveGuild(guild);
            }
            return success;
        }
        return false;
    }

    /**
     * Accepts a player's invite to join a guild
     * @param guildName Name of the guild
     * @param player UUID of the accepting player
     * @return true if successful, false if invite doesn't exist
     */
    public boolean acceptInvite(String guildName, UUID player) {
        Guild guild = guilds.get(guildName);
        if (guild != null && guild.acceptInvite(player)) {
            playerGuilds.put(player, guild);
            storage.saveGuild(guild);
            return true;
        }
        return false;
    }

    public boolean kickMember(String guildName, UUID kicker, UUID kicked) {
        Guild guild = guilds.get(guildName);
        if (guild != null && guild.getLeader().equals(kicker)) {
            if (guild.removeMember(kicked, GuildMemberLeaveEvent.LeaveReason.KICKED)) {
                playerGuilds.remove(kicked);
                storage.saveGuild(guild);
                return true;
            }
        }
        return false;
    }

    /**
     * Gets all registered guilds
     * @return Collection of all guilds
     */
    public Collection<Guild> getGuilds() {
        return Collections.unmodifiableCollection(guilds.values());
    }

    /**
     * Gets a guild by name
     * @param name The name of the guild
     * @return Optional containing the guild if found
     */
    public Optional<Guild> getGuild(String name) {
        return Optional.ofNullable(guilds.get(name));
    }

    public void updateGuildName(Guild guild, String newName) {
        guilds.remove(guild.getName());
        Guild newGuild = new Guild(plugin, newName, guild.getOwner());
        newGuild.setLevel(guild.getLevel());
        newGuild.setExp(guild.getExp());
        guild.getMembers().forEach(member -> {
            newGuild.addMember(member);
            playerGuilds.put(member, newGuild);
        });
        guild.getClaimedChunks().forEach(chunk -> {
            newGuild.claimChunk(chunk);
            claimedChunks.put(chunk, newGuild);
        });
        guilds.put(newName, newGuild);
        storage.saveGuild(newGuild);
    }

    /**
     * Gets the storage implementation being used
     * @return The guild storage instance
     */
    public GuildStorage getStorage() {
        return storage;
    }

    public boolean createAlliance(String allianceName, Guild guild) {
        if (guild == null) {
            return false;
        }
    
        Alliance alliance = new Alliance(allianceName);
        alliance.addMember(guild);
        plugin.getAllianceManager().addAlliance(alliance);
        return true;
    }

    public boolean inviteToAlliance(String allianceName, Guild inviterGuild, Guild invitedGuild) {
        Optional<Alliance> allianceOpt = plugin.getAllianceManager().getAlliance(allianceName);
        if (!allianceOpt.isPresent() || !allianceOpt.get().getMembers().contains(inviterGuild)) {
            return false;
        }
    
        return allianceOpt.get().inviteGuild(invitedGuild);
    }

    public boolean canPasteSchematic(Guild guild, Player player, String structureName) {
        if (!plugin.isAllowStructures()) {
            player.sendMessage(plugin.getMessageManager().getMessage("guild.structure-system-disabled"));
            return false;
        }

        if (!guild.getLeader().equals(player.getUniqueId())) {
            player.sendMessage(plugin.getMessageManager().getMessage("guild.only-leader-can-add-structures"));
            return false;
        }

        FileConfiguration structuresConfig = plugin.getStructuresConfig();
        ConfigurationSection structureSection = structuresConfig.getConfigurationSection("structures." + structureName);
        if (structureSection == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("guild.structure-not-found"));
            return false;
        }

        ConfigurationSection costSection = structureSection.getConfigurationSection("cost");
        for (String materialName : costSection.getKeys(false)) {
            Material material = Material.getMaterial(materialName);
            int amount = costSection.getInt(materialName);
            if (material == null || !player.getInventory().contains(material, amount)) {
                player.sendMessage(plugin.getMessageManager().getMessage("guild.structure-need-materials")
                        .replace("{amount}", String.valueOf(amount))
                        .replace("{material}", materialName));
                return false;
            }
        }

        if (guild.hasBuiltStructure(structureName)) {
            player.sendMessage(plugin.getMessageManager().getMessage("guild.structure-already-built"));
            return false;
        }

        return true;
    }

    public void pasteSchematic(Guild guild, Player player, String structureName) {
        FileConfiguration structuresConfig = plugin.getStructuresConfig();
        ConfigurationSection structureSection = structuresConfig.getConfigurationSection("structures." + structureName);
        if (structureSection == null) {
            player.sendMessage(plugin.getMessageManager().getMessage("guild.structure-not-found"));
            return;
        }

        String schematicName = structureSection.getString("schematic");
        ConfigurationSection costSection = structureSection.getConfigurationSection("cost");

        for (String materialName : costSection.getKeys(false)) {
            Material material = Material.getMaterial(materialName);
            int amount = costSection.getInt(materialName);
            player.getInventory().removeItem(new ItemStack(material, amount));
        }

        guild.addBuiltStructure(structureName);

        // Paste the schematic using WorldEdit/FAWE
        Bukkit.getScheduler().runTask(plugin, () -> {
            try {
                File schematicFile = new File(plugin.getDataFolder(), "schematics/" + schematicName);
                if (!schematicFile.exists()) {
                    player.sendMessage(plugin.getMessageManager().getMessage("guild.schematic-file-not-found"));
                    return;
                }

                ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
                if (format == null) {
                    player.sendMessage(plugin.getMessageManager().getMessage("guild.unsupported-schematic-format"));
                    return;
                }

                try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
                    Clipboard clipboard = reader.read();
                    EditSession editSession = WorldEdit.getInstance().newEditSessionBuilder()
                            .world(BukkitAdapter.adapt(player.getWorld()))
                            .build();
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(BlockVector3.at(player.getLocation().getX(), player.getLocation().getY(), player.getLocation().getZ()))
                            .ignoreAirBlocks(false)
                            .build();
                    Operations.complete(operation);
                    editSession.close();
                }
            } catch (IOException | WorldEditException e) {
                player.sendMessage(plugin.getMessageManager().getMessage("guild.failed-to-paste-schematic"));
                e.printStackTrace();
            }
        });
    }
}