package com.pwing.guilds.storage;

import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.GuildManager;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import java.util.Set;

public interface GuildStorage {
    void saveGuild(Guild guild);
    Guild loadGuild(String name);
    Set<Guild> loadAllGuilds();
    void deleteGuild(String name);
    void saveStorageData(String guildName, ItemStack[] contents);
    ConfigurationSection getStorageData(String guildName);
    GuildManager getGuildManager();

    default void saveAllStorages() {
        getGuildManager().getGuilds().forEach(guild -> 
            saveStorageData(guild.getName(), guild.getStorageContents()));
    }
}
