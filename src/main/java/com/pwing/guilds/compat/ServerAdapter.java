package com.pwing.guilds.compat;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public abstract class ServerAdapter {
    protected final Server server;
    
    public ServerAdapter(Server server) {
        this.server = server;
    }

    public abstract void sendActionBar(Player player, String message);
    public abstract int getPing(Player player);
    public abstract boolean isPrimaryThread();
    public abstract ItemStack getPlayerHeadItem(Player player);
    
    public static ServerAdapter createAdapter(Server server) {
        String serverPackageName = server.getClass().getPackage().getName();
        if (serverPackageName.contains("paper")) {
            return new PaperAdapter(server);
        }
        return new SpigotAdapter(server);
    }
}
