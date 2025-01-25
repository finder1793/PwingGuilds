package com.pwing.guilds.guild;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a guild home location that members can teleport to.
 * Each guild can have multiple named home locations.
 */
public class GuildHome implements ConfigurationSerializable {
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

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> serialized = new HashMap<>();
        serialized.put("name", name);
        serialized.put("location", location.serialize());
        return serialized;
    }

    /**
     * Deserializes a GuildHome from a map of data.
     * @param data the map of data
     * @return the deserialized GuildHome
     */
    public static GuildHome deserialize(Map<String, Object> data) {
        String name = (String) data.get("name");
        Location location = Location.deserialize((Map<String, Object>) data.get("location"));
        return new GuildHome(name, location);
    }
}
