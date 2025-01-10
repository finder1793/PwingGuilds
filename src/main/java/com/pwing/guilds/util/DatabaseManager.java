package com.pwing.guilds.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.ConfigurationSection;

/**
 * Manages database connections and operations for the plugin.
 * Handles connection pooling and database resource management.
 */
public class DatabaseManager {
    private final HikariDataSource dataSource;
    
    /**
     * Creates a new DatabaseManager with the specified configuration
     * @param config The database configuration section
     */
    public DatabaseManager(ConfigurationSection config) {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setJdbcUrl("jdbc:mysql://" +
                config.getString("storage.mysql.host") + ":" +
                config.getInt("storage.mysql.port") + "/" +
                config.getString("storage.mysql.database"));
        hikariConfig.setUsername(config.getString("storage.mysql.username"));
        hikariConfig.setPassword(config.getString("storage.mysql.password"));
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setConnectionTimeout(30000);
        hikariConfig.setIdleTimeout(600000);
        hikariConfig.setMaxLifetime(1800000);
        
        this.dataSource = new HikariDataSource(hikariConfig);
    }
    
    /**
     * Gets the active connection pool data source
     * @return The HikariCP data source
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }
    
    /**
     * Closes the database connection pool and releases resources
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
