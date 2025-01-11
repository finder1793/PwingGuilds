package com.pwing.guilds.message;

import com.pwing.guilds.PwingGuilds;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.Map;

public class MessageManager {
    private final PwingGuilds plugin;
    private final Map<String, String> messages;

    public MessageManager(PwingGuilds plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        loadMessages();
    }

    private void loadMessages() {
        FileConfiguration config = plugin.getConfigManager().getConfig("messages.yml");
        if (config == null) return;

        for (String key : config.getConfigurationSection("messages").getKeys(true)) {
            String path = "messages." + key;
            if (config.isString(path)) {
                messages.put(key, ChatColor.translateAlternateColorCodes('&', config.getString(path)));
            }
        }
    }

    public String getMessage(String key, Object... args) {
        String message = messages.getOrDefault(key, "Missing message: " + key);
        if (args.length > 0) {
            for (int i = 0; i < args.length; i++) {
                message = message.replace("%" + (i + 1), String.valueOf(args[i]));
            }
        }
        return message;
    }

    public String getMessage(String key, Map<String, String> replacements) {
        String message = messages.getOrDefault(key, "Missing message: " + key);
        if (replacements != null) {
            for (Map.Entry<String, String> entry : replacements.entrySet()) {
                message = message.replace("%" + entry.getKey() + "%", entry.getValue());
            }
        }
        return message;
    }

    public void reload() {
        messages.clear();
        loadMessages();
    }
}
