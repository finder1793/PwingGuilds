package com.pwing.guilds.events.custom;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class EventAnnouncer {
    private final PwingGuilds plugin;

    public EventAnnouncer(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    public void announceUpcoming(String eventName, int minutesUntil) {
        String message = switch (minutesUntil) {
            case 60 -> "§6§lGuild Event: §e" + eventName + " §fstarts in §61 hour!";
            case 30 -> "§6§lGuild Event: §e" + eventName + " §fstarts in §630 minutes!";
            case 15 -> "§6§lGuild Event: §e" + eventName + " §fstarts in §615 minutes!";
            case 5 -> "§c§lGuild Event: §e" + eventName + " §fstarts in §c5 minutes!";
            case 1 -> "§c§lGuild Event: §e" + eventName + " §fstarts in §c1 minute!";
            default -> "§6§lGuild Event: §e" + eventName + " §fstarts in §6" + minutesUntil + " minutes!";
        };

        for (Player player : Bukkit.getOnlinePlayers()) {
            player.sendMessage(message);
            player.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(message));
            playAnnouncementSound(player, minutesUntil);
        }
    }

    private void playAnnouncementSound(Player player, int minutesUntil) {
        if (minutesUntil <= 5) {
            player.playSound(player.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 1.0f, 1.0f);
        } else {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        }
    }
}
