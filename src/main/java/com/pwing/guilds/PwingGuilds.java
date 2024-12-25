package com.pwing.guilds;

import com.pwing.guilds.guild.GuildManager;
import com.pwing.guilds.exp.GuildExpManager;
import com.pwing.guilds.listeners.GuildExpListener;
import com.pwing.guilds.listeners.GuildChatListener;
import com.pwing.guilds.listeners.GuildProtectionListener;
import com.pwing.guilds.commands.GuildCommand;
import org.bukkit.plugin.java.JavaPlugin;
import com.pwing.guilds.buffs.GuildBuffManager;

public class PwingGuilds extends JavaPlugin {
    private GuildManager guildManager;
    private GuildExpManager expManager;
    private GuildBuffManager buffManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.guildManager = new GuildManager(this);
        this.expManager = new GuildExpManager(this);
        this.buffManager = new GuildBuffManager(this);
        
        getServer().getPluginManager().registerEvents(new GuildProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildExpListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildChatListener(this), this);
        getCommand("guild").setExecutor(new GuildCommand(this));
    }
    

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public GuildExpManager getExpManager() {
        return expManager;
    }

    public GuildBuffManager getBuffManager() {
        return buffManager;
    }
}