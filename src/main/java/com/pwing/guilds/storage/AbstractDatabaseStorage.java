package com.pwing.guilds.storage;

import com.zaxxer.hikari.HikariDataSource;
import java.sql.Connection;
import java.sql.SQLException;

public abstract class AbstractDatabaseStorage {
    protected final HikariDataSource dataSource;
    
    protected AbstractDatabaseStorage(HikariDataSource dataSource) {
        this.dataSource = dataSource;
    }
    
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
    
    @FunctionalInterface
    protected interface SQLTransaction {
        void execute(Connection conn) throws SQLException;
    }
}
