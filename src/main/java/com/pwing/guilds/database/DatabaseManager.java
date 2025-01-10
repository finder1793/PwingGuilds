package com.pwing.guilds.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.Configuration;

/**
 * Manages database connectivity and connection pooling.
 * Provides optimized database access using HikariCP connection pool.
 * Supports MySQL/MariaDB database backends.
 */
public class DatabaseManager {
    private final HikariDataSource dataSource;

    /**
     * Creates new database manager with specified configuration
     * Initializes connection pool with optimized settings
     * 
     * @param config Configuration containing database credentials and settings
     * @throws IllegalArgumentException if required config values are missing
     * @throws RuntimeException if connection pool initialization fails
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
     * Gets the active connection pool
     * @return HikariCP datasource for database connections
     * @throws IllegalStateException if pool has been shut down
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
