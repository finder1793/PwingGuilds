package com.pwing.guilds.contest.guildcontests;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import java.util.HashMap;
import java.util.Map;
import com.pwing.guilds.guild.Guild;


public class PvPTournamentEvent extends GuildEvent implements Listener {
    private final Map<Guild, Integer> kills = new HashMap<>();

    public PvPTournamentEvent(PwingGuilds plugin, String name, int duration) {
        super(plugin, name, duration);
        this.description = "Guild vs Guild combat tournament! Most kills wins!";
    }

    @Override
    public void start() {
        isActive = true;
        Bukkit.getPluginManager().registerEvents(this, plugin);
        Bukkit.broadcastMessage("§c§lPvP Tournament has begun! Fight for your guild's glory!");
    }

    @Override
    public void end() {
        isActive = false;
        HandlerList.unregisterAll(this);
        updateScores();
        announceWinners();
    }

    @Override
    public void updateScores() {
        scores = new HashMap<>(kills);
    }

    @EventHandler
    public void onPlayerKill(PlayerDeathEvent event) {
        if (!isActive) return;

        Player killer = event.getEntity().getKiller();
        if (killer != null) {
            plugin.getGuildManager().getPlayerGuild(killer.getUniqueId()).ifPresent(guild -> {
                kills.merge(guild, 1, Integer::sum);
                killer.sendMessage("§a+1 Tournament Kill for " + guild.getName());
            });
        }
    }

    private void announceWinners() {
        Guild winner = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);

        if (winner != null) {
            Bukkit.broadcastMessage("§6§lTournament Winners: §e" + winner.getName());
            Bukkit.broadcastMessage("§7With §c" + scores.get(winner) + " §7kills!");
        }
    }
}
