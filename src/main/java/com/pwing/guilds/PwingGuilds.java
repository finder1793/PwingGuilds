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
import com.pwing.guilds.api.EventRegistry;
import com.pwing.guilds.alliance.storage.SQLAllianceStorage;
import com.pwing.guilds.message.MessageManager;
import com.pwing.guilds.storage.SQLGuildStorage;
import com.pwing.guilds.placeholders.GuildPlaceholders;
import com.pwing.guilds.commands.GuildCommand;
import com.pwing.guilds.commands.GuildCommandTabCompleter;
import com.pwing.guilds.commands.GuildAdminCommand;
import com.pwing.guilds.commands.GuildAdminCommandTabCompleter;
import com.pwing.guilds.commands.alliance.AllianceCommand;
import com.pwing.guilds.commands.alliance.AllianceCommandTabCompleter;
import com.pwing.guilds.config.ConfigValidator;
import com.pwing.guilds.contest.guildcontests.GuildEventManager;
import com.pwing.guilds.buffs.GuildBuffManager;
import com.pwing.guilds.storage.GuildStorageManager;
import com.pwing.guilds.storage.GuildBackupManager;
import com.pwing.guilds.storage.GuildBackupListener;
import com.pwing.guilds.rewards.RewardManager;
import com.pwing.guilds.config.ConfigUpdater;
import com.pwing.guilds.config.ConfigManager;
import com.pwing.guilds.config.ConfigMigration;
import com.pwing.guilds.integration.WorldGuardHook;
import com.pwing.guilds.database.DatabaseManager;
import com.pwing.guilds.integrations.skript.SkriptGuildsHook;
import com.pwing.guilds.compat.ItemCompatibilityHandler;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import com.pwing.guilds.compat.ServerAdapter;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.Bukkit;
import java.util.Set;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import org.bukkit.ChatColor;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

/**
 * Main class for the PwingGuilds plugin.
 * Initializes and manages the plugin's components.
 */
public class PwingGuilds extends JavaPlugin {
    /** Storage implementation for guild data */
    private GuildStorage storage;
    /** Manager for guild operations */
    private GuildManager guildManager;
    /** Manager for guild experience systems */
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
    private ItemCompatibilityHandler itemCompatHandler;
    private static PwingGuilds instance;
    private boolean allowStructures;
    private FileConfiguration structuresConfig;
    private HikariDataSource dataSource;

    /**
     * Checks if WorldGuard is available
     * 
     * @return true if WorldGuard is hooked
     */
    public boolean hasWorldGuard() {
        return worldGuardHook != null;
    }

    /**
     * Gets the WorldGuardHook instance.
     * 
     * @return The WorldGuardHook.
     */
    public WorldGuardHook getWorldGuardHook() {
        return worldGuardHook;
    }

    @Override
    public void onEnable() {
        instance = this;
        // Save default config first
        saveDefaultConfig();
        File structuresFile = new File(getDataFolder(), "structures.yml");
        if (!structuresFile.exists()) {
            saveResource("structures.yml", false);
        }
        structuresConfig = YamlConfiguration.loadConfiguration(structuresFile);

        // Ensure structures section is initialized
        if (!structuresConfig.isConfigurationSection("structures")) {
            structuresConfig.createSection("structures");
            saveResource("structures.yml", true);
        }

        // Load the toggle for the structure system
        allowStructures = getConfig().getBoolean("guild-settings.allow-structures", true);

        // Load configuration files
        saveDefaultConfig();
        saveResource("gui.yml", false);

        // Load the GUI configuration
        File guiFile = new File(getDataFolder(), "gui.yml");
        YamlConfiguration guiConfig = YamlConfiguration.loadConfiguration(guiFile);
        getConfig().set("gui", guiConfig.getConfigurationSection("gui"));

        // Register commands first before any other initialization
        try {
            registerCommands();
        } catch (Exception e) {
            getLogger().severe("Failed to register commands: " + e.getMessage());
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // Run config migration before loading configs
        new ConfigMigration(this).migrateConfigs();
        new ConfigUpdater(this).update();

        // Initialize ConfigManager first
        this.configManager = new ConfigManager(this);

        // Check if config validation is enabled
        if (getConfig().getBoolean("validate-config", false)) {
            // Now validate configs with the initialized ConfigManager
            ConfigValidator validator = new ConfigValidator(this);
            if (!validator.validate()) {
                getLogger().severe("Configuration validation failed! Please fix the errors above.");
                getServer().getPluginManager().disablePlugin(this);
                return;
            }
        }

        setupEconomy();
        setupDatabase();

        // Initialize remaining managers
        this.messageManager = new MessageManager(this);

        // Only initialize database manager for MySQL
        if (getConfig().getString("storage.type").equalsIgnoreCase("mysql")) {
            this.databaseManager = new DatabaseManager(getConfig());
        }

        this.eventRegistry = new EventRegistry(this);

        // Initialize appropriate storage type
        if (getConfig().getString("storage.type").equalsIgnoreCase("mysql")) {
            this.storage = new SQLGuildStorage(this, dataSource);
            this.allianceStorage = new SQLAllianceStorage(this, dataSource);
        } else {
            this.storage = new YamlGuildStorage(this);
            this.allianceStorage = new YamlAllianceStorage(this);
        }

        // Initialize GuildManager before loading guilds
        this.guildManager = new GuildManager(this, storage, worldGuardHook);

        // Now load guilds after GuildManager is initialized
        Set<Guild> loadedGuilds = storage.loadAllGuilds();
        getLogger().info("Loaded " + loadedGuilds.size() + " guilds with their claims");

        // Initialize WorldGuard hook if the plugin is present
        if (getServer().getPluginManager().getPlugin("WorldGuard") != null) {
            worldGuardHook = new WorldGuardHook(this);
        }

        this.rewardManager = new RewardManager(this);
        this.eventManager = new GuildEventManager(this);
        this.expManager = new GuildExpManager(this);
        this.buffManager = new GuildBuffManager(this);
        this.allianceManager = new AllianceManager(this, allianceStorage);
        this.storageManager = new GuildStorageManager(this);
        this.serverAdapter = ServerAdapter.createAdapter(getServer());
        this.itemCompatHandler = new ItemCompatibilityHandler(this);

        // Initialize managers
        this.guildManager.initialize();
        this.allianceManager.initialize();

        // Register backup system
        guildBackupManager = new GuildBackupManager(this);
        getServer().getPluginManager().registerEvents(new GuildBackupListener(this, guildBackupManager), this);

        // Register all listeners
        eventRegistry.registerListeners();

        // Setup PlaceholderAPI if available
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new GuildPlaceholders(this).register();
        }

        // Register PvP listener
        getServer().getPluginManager().registerEvents(new GuildPvPListener(this), this);

        // Register inventory click listener
        getServer().getPluginManager().registerEvents(new InventoryClickListener(this), this);

        // Add default config values if they don't exist
        FileConfiguration config = getConfig();
        if (!config.isSet("guild-settings.allow-friendly-fire")) {
            config.set("guild-settings.allow-friendly-fire", false);
            saveConfig();
        }

        // Optional Skript integration
        if (getServer().getPluginManager().getPlugin("Skript") != null) {
            new SkriptGuildsHook(this).registerSkript();
        } else {
            getLogger().info("Skript not found - Skript integration will not be enabled");
        }

        // Register the guildadmin command
        getCommand("guildadmin").setExecutor(new GuildAdminCommand(this));

        getLogger().info("PwingGuilds has been enabled!");
    }

    private void registerCommands() {
        // Create command executors
        GuildCommand guildCmd = new GuildCommand(this);
        GuildAdminCommand adminCmd = new GuildAdminCommand(this);
        AllianceCommand allyCmd = new AllianceCommand(this);

        // Register guild command
        if (getCommand("guild") != null) {
            getCommand("guild").setExecutor(guildCmd);
            getCommand("guild").setTabCompleter(new GuildCommandTabCompleter(this));
        } else {
            throw new IllegalStateException("Failed to register guild command - command not found in plugin.yml");
        }

        // Register admin command
        if (getCommand("guildadmin") != null) {
            getCommand("guildadmin").setExecutor(adminCmd);
            getCommand("guildadmin").setTabCompleter(new GuildAdminCommandTabCompleter(this));
        } else {
            throw new IllegalStateException("Failed to register guildadmin command - command not found in plugin.yml");
        }

        // Register alliance command
        if (getCommand("alliance") != null) {
            getCommand("alliance").setExecutor(allyCmd);
            getCommand("alliance").setTabCompleter(new AllianceCommandTabCompleter(this));
        } else {
            throw new IllegalStateException("Failed to register alliance command - command not found in plugin.yml");
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

    private void setupDatabase() {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + getConfig().getString("storage.mysql.host") + ":" +
                getConfig().getInt("storage.mysql.port") + "/" + getConfig().getString("storage.mysql.database"));
        config.setUsername(getConfig().getString("storage.mysql.username"));
        config.setPassword(getConfig().getString("storage.mysql.password"));
        dataSource = new HikariDataSource(config);
    }

    public HikariDataSource getDataSource() {
        return dataSource;
    }

    // Getters
    /**
     * Gets the AllianceManager instance.
     * 
     * @return The AllianceManager.
     */
    public AllianceManager getAllianceManager() {
        return allianceManager;
    }


    /**
     * Gets the GuildBuffManager instance.
     * 
     * @return The GuildBuffManager.
     */
    public GuildBuffManager getBuffManager() {
        return buffManager;
    }

    /**
     * Gets the ConfigManager instance.
     * 
     * @return The ConfigManager.
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }

    /**
     * Gets the Economy instance.
     * 
     * @return The Economy.
     */
    public Economy getEconomy() {
        return economy;
    }

    /**
     * Gets the GuildEventManager instance.
     * 
     * @return The GuildEventManager.
     */
    public GuildEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Gets the GuildExpManager instance.
     * 
     * @return The GuildExpManager.
     */
    public GuildExpManager getExpManager() {
        return expManager;
    }

    /**
     * Gets the guild manager.
     * 
     * @return The guild manager.
     */
    public GuildManager getGuildManager() {
        return guildManager;
    }

    /**
     * Gets the item compatibility handler.
     * 
     * @return The item compatibility handler.
     */
    public ItemCompatibilityHandler getItemCompatHandler() {
        return itemCompatHandler;
    }

    /**
     * Gets the message manager.
     * 
     * @return The message manager.
     */
    public MessageManager getMessageManager() {
        return messageManager;
    }

    /**
     * Gets the reward manager.
     * 
     * @return The reward manager.
     */
    public RewardManager getRewardManager() {
        return rewardManager;
    }

    /**
     * Gets the guild storage manager.
     * 
     * @return The guild storage manager.
     */
    public GuildStorageManager getStorageManager() {
        return storageManager;
    }

    /**
     * Gets the server adapter.
     * 
     * @return The server adapter.
     */
    public ServerAdapter getServerAdapter() {
        return serverAdapter;
    }

    /**
     * Gets the structures configuration.
     * 
     * @return The structures configuration.
     */
    public FileConfiguration getStructuresConfig() {
        return structuresConfig;
    }

    /**
     * Checks if structures are allowed.
     * 
     * @return True if structures are allowed, false otherwise.
     */
    public boolean isAllowStructures() {
        return allowStructures;
    }

    /**
     * Gets the plugin instance.
     * 
     * @return The plugin instance.
     */
    public static PwingGuilds getInstance() {
        return instance;
    }

    /**
     * Gets a message from the configuration.
     * 
     * @param path The path to the message.
     * @return The message.
     */
    public String getMessage(String path) {
        return ChatColor.translateAlternateColorCodes('&', getConfig().getString(path, ""));
    }

    @Override
    public void onDisable() {
        getLogger().info("Starting final guild data save...");

        if (storage instanceof YamlGuildStorage && guildManager != null) {
            guildManager.getGuilds().forEach(guild -> storage.saveGuild(guild));
        }

        if (storage instanceof SQLGuildStorage) {
            SQLGuildStorage sqlStorage = (SQLGuildStorage) storage;
            sqlStorage.processRemainingQueue();
            sqlStorage.getDataSource().close();
        }

        if (configManager != null) {
            configManager.saveConfigs();
        }

        if (databaseManager != null) {
            databaseManager.shutdown();
        }

        if (eventRegistry != null) {
            eventRegistry.unregisterAll();
        }

        if (guildBackupManager != null) {
            guildBackupManager.shutdown();
        }

        getLogger().info("Guild data save completed!");
    }

    public GuildStorage getGuildStorage() {
        return storage;
    }

    public AllianceStorage getAllianceStorage() {
        return allianceStorage;
    }
}
