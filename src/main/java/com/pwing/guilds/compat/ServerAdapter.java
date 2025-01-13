package com.pwing.guilds.compat;

import org.bukkit.Server;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

/**
 * Adapts server-specific functionality for the PwingGuilds plugin.
 */
public abstract class ServerAdapter {
    /**
     * The server instance.
     */
    protected final Server server;
    
    /**
     * Creates a new server adapter.
     * @param server The server instance.
     */
    public ServerAdapter(Server server) {
        this.server = server;
    }

    /**
     * Sends an action bar message to a player.
     * @param player The player to send the message to.
     * @param message The message to send.
     */
    public abstract void sendActionBar(Player player, String message);

    /**
     * Gets the ping of a player.
     * @param player The player to get the ping of.
     * @return The player's ping.
     */
    public abstract int getPing(Player player);

    /**
     * Checks if the current thread is the primary server thread.
     * @return True if the current thread is the primary thread, false otherwise.
     */
    public abstract boolean isPrimaryThread();

    /**
     * Gets the player head item for a player.
     * @param player The player to get the head item of.
     * @return The player's head item.
     */
    public abstract ItemStack getPlayerHeadItem(Player player);
    
    /**
     * Creates a server adapter for the given server.
     * @param server The server instance.
     * @return The server adapter.
     */
    public static ServerAdapter createAdapter(Server server) {
        String serverPackageName = server.getClass().getPackage().getName();
        if (serverPackageName.contains("paper")) {
            return new PaperAdapter(server);
        }
        return new SpigotAdapter(server);
    }
}
