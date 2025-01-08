package com.pwing.guilds.alliance.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.alliance.Alliance;
import com.pwing.guilds.alliance.AllianceRole;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.*;

public class SQLAllianceStorage implements AllianceStorage {
    private final PwingGuilds plugin;
    private final HikariDataSource dataSource;
    private final Map<String, Alliance> allianceCache = new HashMap<>();

    public SQLAllianceStorage(PwingGuilds plugin, HikariDataSource dataSource) {
        this.plugin = plugin;
        this.dataSource = dataSource;
        initTables();
    }

    private void initTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS alliances (" +
                    "name VARCHAR(32) PRIMARY KEY," +
                    "description TEXT," +
                    "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS alliance_members (" +
                    "alliance_name VARCHAR(32)," +
                    "guild_name VARCHAR(32)," +
                    "PRIMARY KEY (alliance_name, guild_name)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS alliance_roles (" +
                    "alliance_name VARCHAR(32)," +
                    "player_uuid VARCHAR(36)," +
                    "role VARCHAR(16)," +
                    "PRIMARY KEY (alliance_name, player_uuid)" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveAlliance(Alliance alliance) {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Save main alliance data
                try (PreparedStatement ps = conn.prepareStatement(
                        "REPLACE INTO alliances (name, description) VALUES (?, ?)")) {
                    ps.setString(1, alliance.getName());
                    ps.setString(2, alliance.getDescription());
                    ps.executeUpdate();
                }

                // Save members
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO alliance_members (alliance_name, guild_name) VALUES (?, ?)")) {
                    for (var guild : alliance.getMembers()) {
                        ps.setString(1, alliance.getName());
                        ps.setString(2, guild.getName());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                // Save roles
                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO alliance_roles (alliance_name, player_uuid, role) VALUES (?, ?, ?)")) {
                    for (var entry : alliance.getRoles().entrySet()) {
                        ps.setString(1, alliance.getName());
                        ps.setString(2, entry.getKey().toString());
                        ps.setString(3, entry.getValue().name());
                        ps.addBatch();
                    }
                    ps.executeBatch();
                }

                conn.commit();
                allianceCache.put(alliance.getName(), alliance);
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Alliance loadAlliance(String name) {
        if (allianceCache.containsKey(name)) {
            return allianceCache.get(name);
        }

        try (Connection conn = dataSource.getConnection()) {
            Alliance alliance = null;
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT * FROM alliances WHERE name = ?")) {
                ps.setString(1, name);
                ResultSet rs = ps.executeQuery();

                if (rs.next()) {
                    alliance = new Alliance(name);
                    loadAllianceData(alliance, conn);
                    allianceCache.put(name, alliance);
                }
            }
            return alliance;
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to load alliance: " + name);
            e.printStackTrace();
            return null;
        }
    }

    private void loadAllianceData(Alliance alliance, Connection conn) throws SQLException {
        // Load members
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT guild_name FROM alliance_members WHERE alliance_name = ?")) {
            ps.setString(1, alliance.getName());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                plugin.getGuildManager().getGuild(rs.getString("guild_name"))
                    .ifPresent(alliance::addMember);
            }
        }

        // Load roles
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT player_uuid, role FROM alliance_roles WHERE alliance_name = ?")) {
            ps.setString(1, alliance.getName());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                alliance.setRole(
                    UUID.fromString(rs.getString("player_uuid")),
                    AllianceRole.valueOf(rs.getString("role"))
                );
            }
        }
    }

    @Override
    public Set<Alliance> loadAllAlliances() {
        Set<Alliance> alliances = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM alliances")) {

            while (rs.next()) {
                Alliance alliance = loadAlliance(rs.getString("name"));
                if (alliance != null) {
                    alliances.add(alliance);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return alliances;
    }

    @Override
    public void deleteAlliance(String name) {
        allianceCache.remove(name);
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String[] tables = {"alliance_roles", "alliance_members", "alliances"};
                for (String table : tables) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "DELETE FROM " + table + " WHERE alliance_name = ?")) {
                        ps.setString(1, name);
                        ps.executeUpdate();
                    }
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
