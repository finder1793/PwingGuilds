package com.pwing.guilds.integration;

import com.pwing.guilds.PwingGuilds;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.Flag;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;

public class WorldGuardHook {
    private static StateFlag ALLOW_GUILD_CLAIMS;
    private final PwingGuilds plugin;

    public WorldGuardHook(PwingGuilds plugin) {
        this.plugin = plugin;
        registerFlags();
    }

    private void registerFlags() {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        ALLOW_GUILD_CLAIMS = new StateFlag("allow-guild-claims", true);
        registry.register(ALLOW_GUILD_CLAIMS);
    }

    public boolean canClaim(Chunk chunk) {
        Location loc = chunk.getBlock(8, 0, 8).getLocation();
        return WorldGuard.getInstance().getPlatform().getRegionContainer()
            .createQuery()
            .testState(
                BukkitAdapter.adapt(loc),
                WorldGuardPlugin.inst().wrapPlayer(loc.getWorld().getPlayers().get(0)),
                ALLOW_GUILD_CLAIMS
            );
    }
}