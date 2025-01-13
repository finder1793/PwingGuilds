package com.pwing.guilds.storage;

import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import java.util.Set;

/**
 * Interface for guild storage operations.
 */
public interface GuildStorage {
    /**
     * Saves a guild to storage.
     * @param guild The guild to save.
     */
    void saveGuild(Guild guild);

    /**
     * Loads a guild from storage.
     * @param name The name of the guild to load.
     * @return The loaded guild.
     */
    Guild loadGuild(String name);

    /**
     * Loads all guilds from storage.
     * @return A set of all loaded guilds.
     */
    Set<Guild> loadAllGuilds();

    /**
     * Deletes a guild from storage.
     * @param name The name of the guild to delete.
     */
    void deleteGuild(String name);

    /**
     * Saves storage data for a guild.
     * @param guildName The name of the guild.
     * @param contents The storage contents.
     */
    void saveStorageData(String guildName, ItemStack[] contents);

    /**
     * Gets the storage data for a guild.
     * @param guildName The name of the guild.
     * @return The storage data.
     */
    ConfigurationSection getStorageData(String guildName);

    /**
     * Gets the guild manager.
     * @return The guild manager.
     */
    GuildManager getGuildManager();
}
