package com.pwing.guilds.buffs;

import org.bukkit.potion.PotionEffectType;

public class GuildBuff {
    private final String name;
    private final PotionEffectType effectType;
    private final int level;
    private final int cost;
    private final int duration;
    private final String permission;

    public GuildBuff(String name, PotionEffectType effectType, int level, int cost, int duration, String permission) {
        this.name = name;
        this.effectType = effectType;
        this.level = level;
        this.cost = cost;
        this.duration = duration;
        this.permission = permission;
    }

    public String getName() { return name; }
    public PotionEffectType getEffectType() { return effectType; }
    public int getLevel() { return level; }
    public int getCost() { return cost; }
    public int getDuration() { return duration; }
    public String getPermission() { return permission; }
}
