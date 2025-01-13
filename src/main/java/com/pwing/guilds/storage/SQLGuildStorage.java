package com.pwing.guilds.storage;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.guild.ChunkLocation;
import com.pwing.guilds.guild.GuildManager;
import com.pwing.guilds.guild.GuildHome;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

/**
 * SQL implementation of guild data storage.
 * Handles all database operations for storing and retrieving guild data using MySQL/MariaDB.
 * Provides caching and asynchronous saving capabilities for optimal performance.
 */
public class SQLGuildStorage implements GuildStorage {
    private final PwingGuilds plugin;
    private final HikariDataSource dataSource;
    private final Map<String, Guild> guildCache = new HashMap<>();
    private final Queue<Guild> saveQueue = new ConcurrentLinkedQueue<>();
    private static final long SAVE_INTERVAL = 100L;
    private final GuildManager guildManager;

    /**
     * Creates a new SQL storage manager
     * @param plugin Main plugin instance
     * @param dataSource HikariCP datasource for database connections
     */
    public SQLGuildStorage(PwingGuilds plugin, HikariDataSource dataSource) {
        this.plugin = plugin;
        this.guildManager = plugin.getGuildManager();
        this.dataSource = dataSource;
        initTables();
        startAsyncSaveProcessor();
    }

    /**
     * Serializes Bukkit ItemStacks to byte array for storage
     * @param items Array of items to serialize
     * @return Serialized byte array
     * @throws RuntimeException if serialization fails
     */
    private byte[] serializeItems(ItemStack[] items) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream)) {
            dataOutput.writeObject(items);
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Failed to serialize items", e);
        }
    }

    /**
     * Deserializes byte array back to Bukkit ItemStacks
     * @param data Byte array to deserialize
     * @return Array of ItemStacks
     * @throws RuntimeException if deserialization fails
     */
    private ItemStack[] deserializeItems(byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream)) {
            return (ItemStack[]) dataInput.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException("Failed to deserialize items", e);
        }
    }

    /**
     * Initializes required database tables if they don't exist
     * Creates tables for guilds, members, chunks, homes and storage
     */
    private void initTables() {
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS guilds (" +
                    "name VARCHAR(32) PRIMARY KEY," +
                    "owner VARCHAR(36)," +
                    "level INT," +
                    "exp BIGINT," +
                    "bonus_claims INT DEFAULT 0" +
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

            stmt.execute("CREATE TABLE IF NOT EXISTS guild_homes (" +
                    "guild_name VARCHAR(32)," +
                    "home_name VARCHAR(32)," +
                    "world VARCHAR(64)," +
                    "x DOUBLE," +
                    "y DOUBLE," +
                    "z DOUBLE," +
                    "yaw FLOAT," +
                    "pitch FLOAT," +
                    "PRIMARY KEY (guild_name, home_name)" +
                    ")");

            stmt.execute("CREATE TABLE IF NOT EXISTS guild_storage (" +
                    "guild_name VARCHAR(32) PRIMARY KEY," +
                    "contents MEDIUMBLOB" +
                    ")");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Processes any remaining guild saves in the queue
     * Should be called before plugin shutdown
     */
    public void processRemainingQueue() {
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            Guild guild;
            while ((guild = saveQueue.poll()) != null) {
                saveGuildData(guild, conn);
            }
            conn.commit();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to process remaining guild saves!");
            e.printStackTrace();
        }
    }

    /**
     * Starts async task to periodically save queued guild data
     * Runs every SAVE_INTERVAL ticks
     */
    private void startAsyncSaveProcessor() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, () -> {
            try (Connection conn = dataSource.getConnection()) {
                conn.setAutoCommit(false);
                Guild guild;
                while ((guild = saveQueue.poll()) != null) {
                    saveGuildData(guild, conn);
                }
                conn.commit();
            } catch (SQLException e) {
                plugin.getLogger().severe("Failed to process guild save queue!");
                e.printStackTrace();
            }
        }, SAVE_INTERVAL, SAVE_INTERVAL);
    }

    /**
     * Saves guild data to database as a transaction
     * @param guild Guild to save
     * @param conn Active database connection
     * @throws SQLException if database error occurs
     */
    private void saveGuildData(Guild guild, Connection conn) throws SQLException {
        executeTransaction(connection -> {
            // Save main guild data
            try (PreparedStatement ps = connection.prepareStatement(
                    "REPLACE INTO guilds (name, owner, level, exp, bonus_claims) VALUES (?, ?, ?, ?, ?)")) {
                ps.setString(1, guild.getName());
                ps.setString(2, guild.getOwner().toString());
                ps.setInt(3, guild.getLevel());
                ps.setLong(4, guild.getExp());
                ps.setInt(5, guild.getBonusClaims());
                ps.executeUpdate();
            }

            // Clear existing data
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM guild_members WHERE guild_name = ?")) {
                ps.setString(1, guild.getName());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM guild_chunks WHERE guild_name = ?")) {
                ps.setString(1, guild.getName());
                ps.executeUpdate();
            }
            try (PreparedStatement ps = connection.prepareStatement("DELETE FROM guild_homes WHERE guild_name = ?")) {
                ps.setString(1, guild.getName());
                ps.executeUpdate();
            }

            // Save members
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO guild_members (guild_name, uuid) VALUES (?, ?)")) {
                for (UUID member : guild.getMembers()) {
                    ps.setString(1, guild.getName());
                    ps.setString(2, member.toString());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Save chunks with sorting
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO guild_chunks (guild_name, world, x, z) VALUES (?, ?, ?, ?)")) {
                List<ChunkLocation> sortedChunks = guild.getClaimedChunks().stream()
                    .sorted((c1, c2) -> {
                        int worldCompare = c1.getWorldName().compareTo(c2.getWorldName());
                        if (worldCompare != 0) return worldCompare;
                        int xCompare = Integer.compare(c1.getX(), c2.getX());
                        if (xCompare != 0) return xCompare;
                        return Integer.compare(c1.getZ(), c2.getZ());
                    })
                    .collect(Collectors.toList());
                    
                for (ChunkLocation chunk : sortedChunks) {
                    ps.setString(1, guild.getName());
                    ps.setString(2, chunk.getWorldName());
                    ps.setInt(3, chunk.getX());
                    ps.setInt(4, chunk.getZ());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            // Save homes
            try (PreparedStatement ps = connection.prepareStatement(
                    "INSERT INTO guild_homes (guild_name, home_name, world, x, y, z, yaw, pitch) VALUES (?, ?, ?, ?, ?, ?, ?, ?)")) {
                for (Map.Entry<String, GuildHome> entry : guild.getHomes().entrySet()) {
                    Location loc = entry.getValue().getLocation();
                    ps.setString(1, guild.getName());
                    ps.setString(2, entry.getKey());
                    ps.setString(3, loc.getWorld().getName());
                    ps.setDouble(4, loc.getX());
                    ps.setDouble(5, loc.getY());
                    ps.setDouble(6, loc.getZ());
                    ps.setFloat(7, loc.getYaw());
                    ps.setFloat(8, loc.getPitch());
                    ps.addBatch();
                }
                ps.executeBatch();
            }

            conn.commit();
        });
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
        try (PreparedStatement ps = conn.prepareStatement("SELECT uuid FROM guild_members WHERE guild_name = ?")) {
            ps.setString(1, guild.getName());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UUID memberId = UUID.fromString(rs.getString("uuid"));
                guild.addMember(memberId);
                plugin.getGuildManager().getPlayerGuilds().put(memberId, guild);
            }
        }

        // Load chunks
        try (PreparedStatement ps = conn.prepareStatement("SELECT world, x, z FROM guild_chunks WHERE guild_name = ?")) {
            ps.setString(1, guild.getName());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                ChunkLocation chunk = new ChunkLocation(
                        rs.getString("world"),
                        rs.getInt("x"),
                        rs.getInt("z")
                );
                guild.claimChunk(chunk);
            }
        }

        // Load homes
        try (PreparedStatement ps = conn.prepareStatement("SELECT * FROM guild_homes WHERE guild_name = ?")) {
            ps.setString(1, guild.getName());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Location loc = new Location(
                        Bukkit.getWorld(rs.getString("world")),
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch")
                );
                guild.setHome(rs.getString("home_name"), loc);
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
                String[] tables = {"guild_homes", "guild_members", "guild_chunks", "guild_storage", "guilds"};
                for (String table : tables) {
                    try (PreparedStatement ps = conn.prepareStatement("DELETE FROM " + table + " WHERE guild_name = ?")) {
                        ps.setString(1, name);
                        ps.executeUpdate();
                    }
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

    @Override
    public void saveStorageData(String guildName, ItemStack[] contents) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "REPLACE INTO guild_storage (guild_name, contents) VALUES (?, ?)")) {
            ps.setString(1, guildName);
            ps.setBytes(2, serializeItems(contents));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save storage for guild: " + guildName);
            e.printStackTrace();
        }
    }

    @Override
    public ConfigurationSection getStorageData(String guildName) {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                 "SELECT contents FROM guild_storage WHERE guild_name = ?")) {
            ps.setString(1, guildName);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                YamlConfiguration config = new YamlConfiguration();
                config.set("contents", deserializeItems(rs.getBytes("contents")));
                return config;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public GuildManager getGuildManager() {
        return guildManager;
    }

    /**
     * Gets the HikariDataSource instance.
     * 
     * @return The HikariDataSource.
     */
    public HikariDataSource getDataSource() {
        return dataSource;
    }

    /**
     * Executes a database transaction with automatic connection management
     * @param transaction Transaction to execute
     * @throws SQLException if database error occurs
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
     * Functional interface for database transactions.
     */
    @FunctionalInterface
    protected interface SQLTransaction {
        /**
         * Executes a SQL operation.
         * 
         * @param conn The database connection.
         * @throws SQLException if a database access error occurs.
         */
        void execute(Connection conn) throws SQLException;
    }
}


