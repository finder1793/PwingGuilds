package com.pwing.guilds.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.Configuration;

/**
 * Manages database connections and operations for the plugin
 */
public class DatabaseManager {
    private final HikariDataSource dataSource;

    /**
     * Creates a new DatabaseManager with the specified configuration
     * @param config The database configuration
     */
    public DatabaseManager(Configuration config) {
        HikariConfig hikariConfig = new HikariConfig();
        
        hikariConfig.setJdbcUrl("jdbc:mysql://" +
                config.getString("storage.mysql.host") + ":" +
                config.getInt("storage.mysql.port") + "/" +
                config.getString("storage.mysql.database"));
        hikariConfig.setUsername(config.getString("storage.mysql.username"));
        hikariConfig.setPassword(config.getString("storage.mysql.password"));
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMinimumIdle(5);
        hikariConfig.setIdleTimeout(300000);
        hikariConfig.setConnectionTimeout(10000);
        hikariConfig.setMaxLifetime(1800000);
        
        this.dataSource = new HikariDataSource(hikariConfig);
    }

    /**
     * Gets the active database connection pool
     * @return The HikariCP datasource
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Closes the database connection pool
     */
    public void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
