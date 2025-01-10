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

public class EventRegistry {
    private final PwingGuilds plugin;
    private final List<Listener> registeredListeners;

    public EventRegistry(PwingGuilds plugin) {
        this.plugin = plugin;
        this.registeredListeners = new ArrayList<>();
    }

    public void registerListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
        registeredListeners.add(listener);
    }

    public void registerListeners() {
        // Register all plugin listeners
        registerListener(new GuildProtectionListener(plugin));
        registerListener(new GuildExpListener(plugin));
        registerListener(new GuildGUIListener(plugin));
        registerListener(new GuildInventoryListener(plugin));
        registerListener(new GuildChatListener(plugin));
        registerListener(new PlayerJoinQuitListener(plugin));
    }

    public void unregisterAll() {
        registeredListeners.forEach(HandlerList::unregisterAll);
        registeredListeners.clear();
    }

    public List<Listener> getRegisteredListeners() {
        return registeredListeners;
    }
}
