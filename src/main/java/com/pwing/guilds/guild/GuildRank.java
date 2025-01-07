package com.pwing.guilds.guild;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.*;

public class GuildRank implements ConfigurationSerializable {
    private final String name;
    private final Set<String> permissions;
    private final int weight;

    public GuildRank(String name, int weight) {
        this.name = name;
        this.permissions = new HashSet<>();
        this.weight = weight;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("weight", weight);
        data.put("permissions", new ArrayList<>(permissions));
        return data;
    }
}
