package com.pwing.guilds.guild;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkLocation {
    private final String world;
    private final int x;
    private final int z;
    
    // Cache the hashCode
    private final int hash;

    public ChunkLocation(Chunk chunk) {
        this.world = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
        this.hash = calculateHash();
    }

    public ChunkLocation(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
        this.hash = calculateHash();
    }

    public String getWorld() { return world; }
    public int getX() { return x; }
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