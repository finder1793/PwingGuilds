package com.pwing.guilds.compat;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.audience.Audience;

public class PaperAdapter extends ServerAdapter {

    public PaperAdapter(Server server) {
        super(server);
    }

    @Override
    public void sendActionBar(Player player, String message) {
        // Cast to Audience to access Paper's Adventure API
        Audience audience = (Audience) player;
        audience.sendActionBar(Component.text(message));
    }

    @Override
    public int getPing(Player player) {
        return player.getPing(); // Paper method
    }

    @Override
    public boolean isPrimaryThread() {
        return server.isPrimaryThread();
    }

    @Override
    public ItemStack getPlayerHeadItem(Player player) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta != null) {
            meta.setOwningPlayer(player);
            head.setItemMeta(meta);
        }
        return head;
    }
}
