package com.pwing.guilds.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import org.bukkit.Bukkit;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SQLGuildStorage implements GuildStorage {
    private final PwingGuilds plugin;
    private final HikariDataSource dataSource;
    private final Map<String, Guild> guildCache = new HashMap<>();
    private final Queue<Guild> saveQueue = new ConcurrentLinkedQueue<>();
    private static final long SAVE_INTERVAL = 100L;

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
        startAsyncSaveProcessor();
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

    public void startAsyncSaveProcessor() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            Guild guild;
            while ((guild = saveQueue.poll()) != null) {
                try (Connection conn = dataSource.getConnection()) {
                    saveGuildData(guild, conn);
                    guildCache.put(guild.getName(), guild);
                } catch (SQLException e) {
                    plugin.getLogger().severe("Failed to save guild: " + guild.getName());
                    saveQueue.offer(guild);
                }
            }
        }, SAVE_INTERVAL, SAVE_INTERVAL);
    }

    private void saveGuildData(Guild guild, Connection conn) throws SQLException {
        conn.setAutoCommit(false);
        try {
            // Save main guild data
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

            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    @Override
    public void saveGuild(Guild guild) {
        saveQueue.offer(guild);
    }

    @Override
    public Guild loadGuild(String name) {
        if (guildCache.containsKey(name)) {
            return guildCache.get(name);
        }

        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT * FROM guilds WHERE name = ?")) {
            
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            
            if (rs.next()) {
                Guild guild = new Guild(plugin, name, UUID.fromString(rs.getString("owner")));
                loadGuildData(guild, conn);
                guildCache.put(name, guild);
                return guild;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void loadGuildData(Guild guild, Connection conn) throws SQLException {
        // Load members
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT uuid FROM guild_members WHERE guild_name = ?")) {
            ps.setString(1, guild.getName());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                guild.addMember(UUID.fromString(rs.getString("uuid")));
            }
        }

        // Load chunks
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT world, x, z FROM guild_chunks WHERE guild_name = ?")) {
            ps.setString(1, guild.getName());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChunkLocation chunk = new ChunkLocation(
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("z"));
                guild.claimChunk(chunk);
            }
        }
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
        guildCache.remove(name);
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
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
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            } finally {
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
