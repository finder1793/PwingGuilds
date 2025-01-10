package com.pwing.guilds;

import com.pwing.guilds.guild.GuildManager;
import com.pwing.guilds.exp.GuildExpManager;
import com.pwing.guilds.listeners.*;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.storage.GuildStorage;
import com.pwing.guilds.storage.YamlGuildStorage;
import com.pwing.guilds.alliance.AllianceManager;
import com.pwing.guilds.alliance.storage.AllianceStorage;
import com.pwing.guilds.alliance.storage.YamlAllianceStorage;
import com.pwing.guilds.alliance.storage.SQLAllianceStorage;
import com.pwing.guilds.message.MessageManager;
import com.pwing.guilds.storage.SQLGuildStorage;
import com.pwing.guilds.events.custom.GuildEventManager;
import com.pwing.guilds.placeholders.GuildPlaceholders;
import com.pwing.guilds.commands.GuildCommand;
import com.pwing.guilds.commands.GuildCommandTabCompleter;
import com.pwing.guilds.commands.GuildAdminCommand;
import com.pwing.guilds.commands.GuildAdminCommandTabCompleter;
import com.pwing.guilds.commands.alliance.AllianceCommand;
import com.pwing.guilds.commands.alliance.AllianceCommandTabCompleter;
import com.pwing.guilds.config.ConfigValidator;
import com.pwing.guilds.buffs.GuildBuffManager;
import com.pwing.guilds.storage.GuildStorageManager;
import com.pwing.guilds.storage.GuildBackupManager;
import com.pwing.guilds.storage.GuildBackupListener;
import com.pwing.guilds.rewards.RewardManager;
import com.pwing.guilds.config.ConfigUpdater;
import com.pwing.guilds.config.ConfigManager;
import com.pwing.guilds.config.ConfigMigration;
import com.pwing.guilds.integration.WorldGuardHook;
import com.pwing.guilds.events.EventRegistry;
import com.pwing.guilds.database.DatabaseManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.pwing.guilds.compat.ServerAdapter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import java.util.Set;

public class PwingGuilds extends JavaPlugin {
    private GuildStorage storage;
    private GuildManager guildManager;
    private GuildExpManager expManager;
    private GuildBuffManager buffManager;
    private RewardManager rewardManager;
    private Economy economy;
    private GuildEventManager eventManager;
    private AllianceManager allianceManager;
    private AllianceStorage allianceStorage;
    private GuildStorageManager storageManager;
    private WorldGuardHook worldGuardHook;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private EventRegistry eventRegistry;
    private ServerAdapter serverAdapter;
    private GuildBackupManager guildBackupManager;
    private MessageManager messageManager;

    public boolean hasWorldGuard() {
        return worldGuardHook != null;
    }
    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }
    @Override
    public void onEnable() {
        saveDefaultConfig();
        
        // Run config migration before loading configs
        new ConfigMigration(this).migrateConfigs();
        new ConfigUpdater(this).update();

        ConfigValidator validator = new ConfigValidator(this);
        if (!validator.validate()) {
            getLogger().severe("Configuration validation failed! Please fix the errors above.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        setupEconomy();

        // Initialize managers
        this.configManager = new ConfigManager(this);
        this.messageManager = new MessageManager(this);
        this.databaseManager = new DatabaseManager(getConfig());
        this.eventRegistry = new EventRegistry(this);

        // Initialize appropriate storage type
        if (getConfig().getString("storage.type").equalsIgnoreCase("mysql")) {
            this.storage = new SQLGuildStorage(this, databaseManager.getDataSource());
            this.allianceStorage = new SQLAllianceStorage(this, databaseManager.getDataSource());
        } else {
            this.storage = new YamlGuildStorage(this);
            this.allianceStorage = new YamlAllianceStorage(this);
        }

        Set<Guild> loadedGuilds = storage.loadAllGuilds();
        getLogger().info("Loaded " + loadedGuilds.size() + " guilds with their claims");

        // Initialize WorldGuard hook if the plugin is present
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardHook = new WorldGuardHook(this);
        }

        this.guildManager = new GuildManager(this, storage, worldGuardHook);
        this.rewardManager = new RewardManager(this);
        this.eventManager = new GuildEventManager(this);
        this.expManager = new GuildExpManager(this);
        this.buffManager = new GuildBuffManager(this);
        this.allianceManager = new AllianceManager(this, allianceStorage);
        this.storageManager = new GuildStorageManager(this);
        this.serverAdapter = ServerAdapter.createAdapter(getServer());

        // Initialize managers
        this.guildManager.initialize();
        this.allianceManager.initialize();

        // Register backup system
        guildBackupManager = new GuildBackupManager(this);
        getServer().getPluginManager().registerEvents(new GuildBackupListener(this, guildBackupManager), this);

        // Register all listeners
        eventRegistry.registerListeners();

        // Register commands
        getCommand("guild").setExecutor(new GuildCommand(this));
        getCommand("guild").setTabCompleter(new GuildCommandTabCompleter(this));
        
        getCommand("guildadmin").setExecutor(new GuildAdminCommand(this));
        getCommand("guildadmin").setTabCompleter(new GuildAdminCommandTabCompleter(this));
        
        getCommand("alliance").setExecutor(new AllianceCommand(this));
        getCommand("alliance").setTabCompleter(new AllianceCommandTabCompleter(this));
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
    public ServerAdapter getServerAdapter() {
        return serverAdapter;
    }
    public ConfigManager getConfigManager() {
        return configManager;
    }
    public MessageManager getMessageManager() {
        return messageManager;
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
        
        // Clean up resources
        configManager.saveConfigs();
        databaseManager.shutdown();
        eventRegistry.unregisterAll();

        if (guildBackupManager != null) {
            guildBackupManager.shutdown();
        }

        getLogger().info("Guild data save completed!");
    }
}







