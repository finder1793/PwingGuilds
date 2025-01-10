package com.pwing.guilds.guild;

import org.bukkit.Chunk;
import org.bukkit.World;

/**
 * Represents a location of a chunk in the world
 */
public class ChunkLocation {
    private final String world;
    private final int x;
    private final int z;
    
    // Cache the hashCode
    private final int hash;

    /**
     * Creates a new ChunkLocation from a Bukkit Chunk
     * @param chunk The Bukkit chunk
     */
    public ChunkLocation(Chunk chunk) {
        this.world = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.hash = calculateHash();
    }

    /**
     * Creates a new ChunkLocation from coordinates
     * @param world The world name
     * @param x The x coordinate
     * @param z The z coordinate
     */
    public ChunkLocation(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.hash = calculateHash();
    }

    /**
     * Gets the world name
     * @return The world name
     */
    public String getWorld() { return world; }

    /**
     * Gets the x coordinate
     * @return The x coordinate
     */
    public int getX() { return x; }

    /**
     * Gets the z coordinate
     * @return The z coordinate
     */
    public int getZ() { return z; }

    private int calculateHash() {
        return 31 * (31 * world.hashCode() + x) + z;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        ChunkLocation other = (ChunkLocation) obj;
        return x == other.x && z == other.z && world.equals(other.world);
    }
}