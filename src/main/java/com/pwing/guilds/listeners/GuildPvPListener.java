package com.pwing.guilds.listeners;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.guild.Guild;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import java.util.Optional;

public class GuildPvPListener implements Listener {
    private final PwingGuilds plugin;

    public GuildPvPListener(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerDamage(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player) || !(event.getDamager() instanceof Player)) {
            return;
        }

        Player victim = (Player) event.getEntity();
        Player attacker = (Player) event.getDamager();
        
        // Check if in guild territory
        Optional<Guild> territoryGuild = plugin.getGuildManager().getGuildByChunk(victim.getLocation().getChunk());
        if (territoryGuild.isPresent() && !territoryGuild.get().isPvPAllowed(attacker, victim)) {
            event.setCancelled(true);
            attacker.sendMessage("§cPvP is not allowed in this guild territory!");
        }
    }
}