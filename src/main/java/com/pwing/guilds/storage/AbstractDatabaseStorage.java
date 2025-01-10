package com.pwing.guilds.storage;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstract base class for database storage implementations.
 */
public abstract class AbstractDatabaseStorage {
    
    /** The data source for database connections */
    protected final HikariDataSource dataSource;
    
    /**
     * Creates a new database storage with the given data source
     * @param dataSource The HikariCP data source to use
     */
    protected AbstractDatabaseStorage(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    /**
     * Executes a database transaction with automatic connection management
     * @param transaction The transaction to execute
     * @throws SQLException if a database error occurs
     */
    protected void executeTransaction(SQLTransaction transaction) throws SQLException {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                transaction.execute(conn);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        }
    }
    
    /**
     * Interface for database transactions
     */
    @FunctionalInterface
    protected interface SQLTransaction {
        /**
         * Executes the transaction using the provided connection
         * @param conn The database connection
         * @throws SQLException if a database error occurs
         */
        void execute(Connection conn) throws SQLException;
    }
}
