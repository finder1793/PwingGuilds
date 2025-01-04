package com.pwing.guilds.guild;

import org.bukkit.Location;

public class GuildHome {
    private final String name;
    private final Location location;

    public GuildHome(String name, Location location) {
        this.name = name;
        this.location = location;
    }

    public String getName() { return name; }
    public Location getLocation() { return location; }
}
