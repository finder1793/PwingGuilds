package com.pwing.guilds.guild;

import org.bukkit.Chunk;
import org.bukkit.World;

public class ChunkLocation {
    private final String world;
    private final int x;
    private final int z;

    public ChunkLocation(Chunk chunk) {
        this.world = chunk.getWorld().getName();
        this.x = chunk.getX();
        this.z = chunk.getZ();
    }

    public ChunkLocation(String world, int x, int z) {
        this.world = world;
        this.x = x;
        this.z = z;
    }

    public String getWorld() { return world; }
    public int getX() { return x; }
    public int getZ() { return z; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ChunkLocation)) return false;
        ChunkLocation that = (ChunkLocation) o;
        return x == that.x && z == that.z && world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return 31 * (31 * world.hashCode() + x) + z;
    }
}