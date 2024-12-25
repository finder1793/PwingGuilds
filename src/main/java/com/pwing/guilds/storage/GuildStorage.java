package com.pwing.guilds.storage;

import com.pwing.guilds.guild.Guild;
import java.util.Set;
import java.util.UUID;

public interface GuildStorage {
    void saveGuild(Guild guild);
    Guild loadGuild(String name);
    void deleteGuild(String name);
    Set<Guild> loadAllGuilds();
}
