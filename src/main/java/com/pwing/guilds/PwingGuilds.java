package com.pwing.guilds;

import com.pwing.guilds.guild.GuildManager;
import com.pwing.guilds.exp.GuildExpManager;
import com.pwing.guilds.listeners.GuildExpListener;
import com.pwing.guilds.listeners.GuildChatListener;
import com.pwing.guilds.listeners.GuildProtectionListener;
import  com.pwing.guilds.storage.GuildStorage;
import com.pwing.guilds.storage.YamlGuildStorage;
import com.pwing.guilds.storage.SQLGuildStorage;
import com.pwing.guilds.events.custom.GuildEventManager;
import com.pwing.guilds.placeholders.GuildPlaceholders;
import com.pwing.guilds.commands.GuildCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import com.pwing.guilds.config.ConfigValidator;
import com.pwing.guilds.buffs.GuildBuffManager;
import com.pwing.guilds.rewards.RewardManager;
import com.pwing.guilds.config.ConfigUpdater;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;

public class PwingGuilds extends JavaPlugin {
    private GuildStorage storage;
    private GuildManager guildManager;
    private GuildExpManager expManager;
    private GuildBuffManager buffManager;
    private RewardManager rewardManager;
    private Economy economy;
    private GuildEventManager eventManager;
    @Override
    public void onEnable() {
        saveDefaultConfig();
        new ConfigUpdater(this).update();

        ConfigValidator validator = new ConfigValidator(this);
        if (!validator.validate()) {
            getLogger().severe("Configuration validation failed! Please fix the errors above.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupEconomy();
        // Initialize storage based on config
        if (getConfig().getString("storage.type").equalsIgnoreCase("mysql")) {
            this.storage = new SQLGuildStorage(this);
        } else {
            this.storage = new YamlGuildStorage(this);
        }

        // Initialize managers
        this.guildManager = new GuildManager(this, storage);
        this.rewardManager = new RewardManager(this);
        this.eventManager = new GuildEventManager(this);
        this.expManager = new GuildExpManager(this);
        this.buffManager = new GuildBuffManager(this);
        
        getServer().getPluginManager().registerEvents(new GuildProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildExpListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildChatListener(this), this);
        getCommand("guild").setExecutor(new GuildCommand(this));

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GuildPlaceholders(this).register();
        }
    }
    
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return true;
    }

    public GuildManager getGuildManager() {
        return guildManager;
    }

    public GuildExpManager getExpManager() {
        return expManager;
    }

    public GuildEventManager getEventManager() {
        return eventManager;
    }
    public GuildBuffManager getBuffManager() {
        return buffManager;
    }

    public RewardManager getRewardManager() {
        return rewardManager;
    }

    public Economy getEconomy() {
        return economy;
    }
}