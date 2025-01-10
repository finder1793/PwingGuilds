package com.pwing.guilds.events;

import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.listeners.GuildProtectionListener;
import com.pwing.guilds.listeners.GuildExpListener;
import com.pwing.guilds.listeners.GuildGUIListener;
import com.pwing.guilds.listeners.GuildInventoryListener;
import com.pwing.guilds.listeners.GuildChatListener;
import com.pwing.guilds.listeners.PlayerJoinQuitListener;
import org.bukkit.event.Listener;
import org.bukkit.event.HandlerList;

import java.util.ArrayList;
import java.util.List;

/**
 * Manages registration and tracking of event listeners
 */
public class EventRegistry {
    private final PwingGuilds plugin;
    private final List<Listener> registeredListeners;

    /**
     * Creates a new EventRegistry
     * @param plugin The plugin instance
     */
    public EventRegistry(PwingGuilds plugin) {
        this.plugin = plugin;
        this.registeredListeners = new ArrayList<>();
    }

    /**
     * Registers a single event listener
     * @param listener The listener to register
     */
    public void registerListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        registeredListeners.add(listener);
    }

    /**
     * Registers all default plugin listeners
     */
    public void registerListeners() {
        // Register all plugin listeners
        registerListener(new GuildProtectionListener(plugin));
        registerListener(new GuildExpListener(plugin));
        registerListener(new GuildGUIListener(plugin));
        registerListener(new GuildInventoryListener(plugin));
        registerListener(new GuildChatListener(plugin));
        registerListener(new PlayerJoinQuitListener(plugin));
    }

    /**
     * Unregisters all plugin listeners
     */
    public void unregisterAll() {
        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();
    }

    /**
     * Gets the list of registered listeners
     * @return The list of registered listeners
     */
    public List<Listener> getRegisteredListeners() {
        return registeredListeners;
    }
}
