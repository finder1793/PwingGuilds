package com.pwing.guilds;

import com.pwing.guilds.guild.GuildManager;
import com.pwing.guilds.exp.GuildExpManager;
import com.pwing.guilds.listeners.*;
import com.pwing.guilds.storage.GuildStorage;
import com.pwing.guilds.storage.YamlGuildStorage;
import com.pwing.guilds.alliance.AllianceManager;
import com.pwing.guilds.alliance.storage.AllianceStorage;
import com.pwing.guilds.alliance.storage.YamlAllianceStorage;
import com.pwing.guilds.alliance.storage.SQLAllianceStorage;
import com.pwing.guilds.storage.SQLGuildStorage;
import com.pwing.guilds.events.custom.GuildEventManager;
import com.pwing.guilds.placeholders.GuildPlaceholders;
import com.pwing.guilds.commands.GuildCommand;
import com.pwing.guilds.commands.GuildAdminCommand;
import com.pwing.guilds.config.ConfigValidator;
import com.pwing.guilds.buffs.GuildBuffManager;
import com.pwing.guilds.storage.GuildStorageManager;
import com.pwing.guilds.storage.GuildBackupManager;
import com.pwing.guilds.storage.GuildBackupListener;
import com.pwing.guilds.rewards.RewardManager;
import com.pwing.guilds.gui.GuildGUIListener;
import com.pwing.guilds.config.ConfigUpdater;
import com.pwing.guilds.integration.WorldGuardHook;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;

public class PwingGuilds extends JavaPlugin {
    private GuildStorage storage;
    private GuildManager guildManager;
    private GuildExpManager expManager;
    private GuildBuffManager buffManager;
    private RewardManager rewardManager;
    private Economy economy;
    private GuildEventManager eventManager;
    private WorldGuardHook worldGuardHook;
    private AllianceManager allianceManager;
    private AllianceStorage allianceStorage;
    private GuildStorageManager storageManager;
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

        // Initialize appropriate storage type
        if (getConfig().getString("storage.type").equalsIgnoreCase("mysql")) {
            this.storage = new SQLGuildStorage(this);
            this.allianceStorage = new SQLAllianceStorage(this, ((SQLGuildStorage) storage).getDataSource());
        } else {
            this.storage = new YamlGuildStorage(this);
            this.allianceStorage = new YamlAllianceStorage(this);
        }

        this.worldGuardHook = new WorldGuardHook(this);
        this.guildManager = new GuildManager(this, storage, worldGuardHook);
        this.rewardManager = new RewardManager(this);
        this.eventManager = new GuildEventManager(this);
        this.expManager = new GuildExpManager(this);
        this.buffManager = new GuildBuffManager(this);
        this.allianceManager = new AllianceManager(this, allianceStorage);
        this.storageManager = new GuildStorageManager(this);

        // Initialize managers
        this.guildManager.initialize();
        this.allianceManager.initialize();

        // Register backup system
        GuildBackupManager backupManager = new GuildBackupManager(this);
        getServer().getPluginManager().registerEvents(new GuildBackupListener(this, backupManager), this);

        // Register all listeners
        getServer().getPluginManager().registerEvents(new GuildPlayerListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildExpListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildChatListener(this), this);
        getServer().getPluginManager().registerEvents(new GuildGUIListener(this), this);
        getServer().getPluginManager().registerEvents(new AllianceListener(this), this);

        // Register commands
        getCommand("guild").setExecutor(new GuildCommand(this));
        getCommand("guildadmin").setExecutor(new GuildAdminCommand(this));

        // Setup PlaceholderAPI if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GuildPlaceholders(this).register();
        }

        getLogger().info("PwingGuilds has been enabled!");
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

    // Getters
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

    public AllianceManager getAllianceManager() {
        return allianceManager;
    }

    public AllianceStorage getAllianceStorage() {
        return allianceStorage;
    }
    public GuildStorageManager getStorageManager() {
    return storageManager;
    }
    @Override
    public void onDisable() {
        getLogger().info("Starting final guild data save...");
        
        // Force sync save for YAML storage
        if (storage instanceof YamlGuildStorage) {
            guildManager.getGuilds().forEach(guild -> storage.saveGuild(guild));
        }
        
        // Process remaining queue and cleanup for SQL storage
        if (storage instanceof SQLGuildStorage) {
            SQLGuildStorage sqlStorage = (SQLGuildStorage) storage;
            sqlStorage.processRemainingQueue();
            sqlStorage.getDataSource().close();
        }
        
        getLogger().info("Guild data save completed!");
    }
}





