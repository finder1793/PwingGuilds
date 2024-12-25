package com.pwing.guilds.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SQLGuildStorage implements GuildStorage {
    private final PwingGuilds plugin;
    private final HikariDataSource dataSource;

    public SQLGuildStorage(PwingGuilds plugin) {
        this.plugin = plugin;
        
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://" + 
            plugin.getConfig().getString("storage.mysql.host") + ":" +
            plugin.getConfig().getInt("storage.mysql.port") + "/" +
            plugin.getConfig().getString("storage.mysql.database"));
        config.setUsername(plugin.getConfig().getString("storage.mysql.username"));
        config.setPassword(plugin.getConfig().getString("storage.mysql.password"));
        config.setMaximumPoolSize(10);
        
        this.dataSource = new HikariDataSource(config);
        initTables();
    }

    private void initTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            
            stmt.execute("CREATE TABLE IF NOT EXISTS guilds (" +
                    "name VARCHAR(32) PRIMARY KEY," +
                    "owner VARCHAR(36)," +
                    "level INT," +
                    "exp BIGINT" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS guild_members (" +
                    "guild_name VARCHAR(32)," +
                    "uuid VARCHAR(36)," +
                    "PRIMARY KEY (guild_name, uuid)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS guild_chunks (" +
                    "guild_name VARCHAR(32)," +
                    "world VARCHAR(64)," +
                    "x INT," +
                    "z INT," +
                    "PRIMARY KEY (guild_name, world, x, z)" +
                    ")");
            
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void saveGuild(Guild guild) {
        try (Connection conn = dataSource.getConnection()) {
            // Save guild data
            try (PreparedStatement ps = conn.prepareStatement(
                    "REPLACE INTO guilds (name, owner, level, exp) VALUES (?, ?, ?, ?)")) {
                ps.setString(1, guild.getName());
                ps.setString(2, guild.getOwner().toString());
                ps.setInt(3, guild.getLevel());
                ps.setLong(4, guild.getExp());
                ps.executeUpdate();
            }

            // Save members
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO guild_members (guild_name, uuid) VALUES (?, ?)")) {
                for (UUID member : guild.getMembers()) {
                    ps.setString(1, guild.getName());
                    ps.setString(2, member.toString());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Save chunks
            try (PreparedStatement ps = conn.prepareStatement(
                    "INSERT INTO guild_chunks (guild_name, world, x, z) VALUES (?, ?, ?, ?)")) {
                for (ChunkLocation chunk : guild.getClaimedChunks()) {
                    ps.setString(1, guild.getName());
                    ps.setString(2, chunk.getWorld());
                    ps.setInt(3, chunk.getX());
                    ps.setInt(4, chunk.getZ());
                    ps.addBatch();
                }
                ps.executeBatch();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public Guild loadGuild(String name) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM guilds WHERE name = ?")) {
            
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                UUID owner = UUID.fromString(rs.getString("owner"));
                Guild guild = new Guild(plugin, name, owner);
                
                // Load members
                try (PreparedStatement memberPs = conn.prepareStatement(
                        "SELECT uuid FROM guild_members WHERE guild_name = ?")) {
                    memberPs.setString(1, name);
                    ResultSet memberRs = memberPs.executeQuery();
                    while (memberRs.next()) {
                        guild.addMember(UUID.fromString(memberRs.getString("uuid")));
                    }
                }

                // Load chunks
                try (PreparedStatement chunkPs = conn.prepareStatement(
                        "SELECT world, x, z FROM guild_chunks WHERE guild_name = ?")) {
                    chunkPs.setString(1, name);
                    ResultSet chunkRs = chunkPs.executeQuery();
                    while (chunkRs.next()) {
                        ChunkLocation chunk = new ChunkLocation(
                                chunkRs.getString("world"),
                                chunkRs.getInt("x"),
                                chunkRs.getInt("z"));
                        guild.claimChunk(chunk);
                    }
                }

                return guild;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Set<Guild> loadAllGuilds() {
        Set<Guild> guilds = new HashSet<>();
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT name FROM guilds")) {
            
            while (rs.next()) {
                Guild guild = loadGuild(rs.getString("name"));
                if (guild != null) {
                    guilds.add(guild);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return guilds;
    }

    @Override
    public void deleteGuild(String name) {
        try (Connection conn = dataSource.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM guilds WHERE name = ?")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM guild_members WHERE guild_name = ?")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = conn.prepareStatement("DELETE FROM guild_chunks WHERE guild_name = ?")) {
                ps.setString(1, name);
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
