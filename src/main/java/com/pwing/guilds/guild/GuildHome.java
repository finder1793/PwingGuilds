package com.pwing.guilds.guild;

import org.bukkit.Location;

/**
 * Represents a guild home location that members can teleport to.
 * Each guild can have multiple named home locations.
 */
public class GuildHome {
    private final String name;
    private final Location location;

    /**
     * Creates a new guild home location
     * @param name The name of the home location
     * @param location The location of the home
     */
    public GuildHome(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    /**
     * Gets the name of this home location
     * @return The home's name
     */
    public String getName() { return name; }

    /**
     * Gets the location of this home
     * @return The home's location
     */
    public Location getLocation() { return location; }
}
