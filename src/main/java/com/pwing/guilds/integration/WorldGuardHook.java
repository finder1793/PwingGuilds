package com.pwing.guilds.integration;

import com.pwing.guilds.PwingGuilds;
import com.sk89q.worldguard.WorldGuard;
import com.sk89q.worldguard.protection.flags.StateFlag;
import com.sk89q.worldguard.protection.flags.registry.FlagRegistry;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import org.bukkit.Chunk;
import org.bukkit.Location;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.ServerLoadEvent;

public class WorldGuardHook implements Listener {
    private static StateFlag ALLOW_GUILD_CLAIMS;
    private final PwingGuilds plugin;

    public WorldGuardHook(PwingGuilds plugin) {
        this.plugin = plugin;
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    @EventHandler
    public void onServerLoad(ServerLoadEvent event) {
        FlagRegistry registry = WorldGuard.getInstance().getFlagRegistry();
        try {
            StateFlag flag = new StateFlag("allow-guild-claims", true);
            registry.register(flag);
            ALLOW_GUILD_CLAIMS = flag;
        } catch (Exception e) {
            ALLOW_GUILD_CLAIMS = (StateFlag) registry.get("allow-guild-claims");
        }
    }

        public boolean canClaim(Chunk chunk) {
            Location loc = chunk.getBlock(8, 0, 8).getLocation();
            try {
                return WorldGuard.getInstance().getPlatform().getRegionContainer()
                    .createQuery()
                    .testState(
                        BukkitAdapter.adapt(loc),
                        WorldGuardPlugin.inst().wrapPlayer(chunk.getWorld().getPlayers().get(0)),
                        ALLOW_GUILD_CLAIMS
                    );
            } catch (Exception e) {
                // If WorldGuard check fails, default to allowing claims
                return true;
            }
        }
}
