package com.pwing.guilds.guild;

import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * Represents a location of a chunk in the world
 */
public class ChunkLocation {
    private final String worldName;
    private final int x;
    private final int z;
    
    // Cache the hashCode
    private final int hash;

    /**
     * Creates a new ChunkLocation from a Bukkit Chunk
     * @param chunk The Bukkit chunk
     */
    public ChunkLocation(Chunk chunk) {
        this.worldName = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.hash = calculateHash();
    }

    /**
     * Creates a new ChunkLocation from coordinates
     * @param worldName The world name
     * @param x The x coordinate
     * @param z The z coordinate
     */
    public ChunkLocation(String worldName, int x, int z) {
        this.worldName = worldName;
        this.x = x;
        this.z = z;
        this.hash = calculateHash();
    }

    /**
     * Gets the world name
     * @return The world name
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Gets the world
     * @return The Bukkit world
     */
    public World getWorld() {
        return Bukkit.getWorld(worldName);
    }

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

    /**
     * Converts this chunk location to a Bukkit Location
     * @return Location at the center of this chunk
     */
    public Location toLocation() {
        World world = getWorld();
        if (world == null) {
            throw new IllegalStateException("World '" + worldName + "' not found");
        }
        return new Location(world, x * 16, 64, z * 16);
    }

    /**
     * Checks if the given ChunkLocation is adjacent to this ChunkLocation.
     *
     * @param other the ChunkLocation to check adjacency with
     * @return true if the given ChunkLocation is adjacent, false otherwise
     */
    public boolean isAdjacent(ChunkLocation other) {
        return this.worldName.equals(other.worldName) &&
                (Math.abs(this.x - other.x) == 1 && this.z == other.z ||
                Math.abs(this.z - other.z) == 1 && this.x == other.x);
    }

    private int calculateHash() {
        return 31 * (31 * worldName.hashCode() + x) + z;
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
        return x == other.x && z == other.z && worldName.equals(other.worldName);
    }

    @Override 
    public String toString() {
        return String.format("%s(%d,%d)", worldName, x, z);
    }
}