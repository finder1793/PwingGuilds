package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Handles experience gain events for guilds
 * Awards exp for block breaking and mob kills
 */
public class GuildExpListener implements Listener {
    private final PwingGuilds plugin;

    /**
     * Creates a new guild exp listener
     * @param plugin Plugin instance
     */
    public GuildExpListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Awards exp for breaking blocks
     * @param event Block break event
     */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!plugin.getConfig().getBoolean("exp-sources.blocks.enabled")) return;
        
        plugin.getGuildManager().getPlayerGuild(event.getPlayer().getUniqueId()).ifPresent(guild -> {
            long exp = plugin.getExpManager().calculateBlockExp(event.getBlock());
            if (guild.addExp(exp)) {
                event.getPlayer().sendMessage("§aYour guild has reached level " + guild.getLevel() + "!");
            }
        });
    }

    /**
     * Awards exp for killing entities
     * @param event Entity death event
     */
    @EventHandler
    public void onEntityKill(EntityDeathEvent event) {
        if (event.getEntity().getKiller() == null) return;
        if (!plugin.getConfig().getBoolean("exp-sources.vanilla-mobs.enabled") && 
            !plugin.getConfig().getBoolean("exp-sources.mythicmobs.enabled")) return;

        plugin.getGuildManager().getPlayerGuild(event.getEntity().getKiller().getUniqueId()).ifPresent(guild -> {
            long exp = plugin.getExpManager().calculateMobExp(event.getEntity());
            if (guild.addExp(exp)) {
                event.getEntity().getKiller().sendMessage("§aYour guild has reached level " + guild.getLevel() + "!");
            }
        });
    }
}