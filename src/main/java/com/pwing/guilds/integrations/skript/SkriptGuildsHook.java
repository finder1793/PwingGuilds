package com.pwing.guilds.integrations.skript;

import ch.njol.skript.Skript;
import ch.njol.skript.lang.ExpressionType;
import ch.njol.skript.lang.util.SimpleEvent;
import ch.njol.skript.registrations.EventValues;
import ch.njol.skript.util.Getter;
import com.pwing.guilds.PwingGuilds;
import com.pwing.guilds.api.GuildCreateEvent;
import com.pwing.guilds.api.GuildDeleteEvent;
import com.pwing.guilds.api.GuildLevelUpEvent;
import com.pwing.guilds.api.GuildMemberJoinEvent;
import com.pwing.guilds.api.GuildMemberLeaveEvent;
import com.pwing.guilds.api.GuildRenameEvent;
import com.pwing.guilds.guild.Guild;
import com.pwing.guilds.integrations.skript.conditions.*;
import com.pwing.guilds.integrations.skript.effects.*;
import com.pwing.guilds.integrations.skript.expressions.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Integrates Skript with the PwingGuilds plugin.
 */
public class SkriptGuildsHook {
    
    private final PwingGuilds plugin;
    
    /**
     * Creates a new SkriptGuildsHook.
     * @param plugin The plugin instance.
     */
    public SkriptGuildsHook(PwingGuilds plugin) {
        this.plugin = plugin;
    }

    /**
     * Registers Skript hooks for the PwingGuilds plugin.
     */
    public void registerSkript() {
        if (!isSkriptInstalled()) {
            return;
        }

        try {
            // Register conditions
            Skript.registerCondition(CondPlayerInGuild.class, "player %player% (is|has) [a] [member of] [a] guild");
            Skript.registerCondition(CondGuildExists.class, "guild [with] [name] %string% exists");

            // Register effects 
            Skript.registerEffect(EffCreateGuild.class, "create [a] [new] guild [named] %string% with leader %player%");
            Skript.registerEffect(EffDeleteGuild.class, "delete guild %string%");
            Skript.registerEffect(EffJoinGuild.class, "make %player% join guild %string%");
            Skript.registerEffect(EffLeaveGuild.class, "make %player% leave [their] guild");

            // Register expressions
            Skript.registerExpression(ExprGuildOfPlayer.class, Guild.class, ExpressionType.PROPERTY,
                    "[the] guild of %player%", "%player%'s guild");
            Skript.registerExpression(ExprGuildLevel.class, Number.class, ExpressionType.PROPERTY,
                    "[the] level of guild %string%", "guild %string%'s level");
            Skript.registerExpression(ExprGuildMembers.class, Player.class, ExpressionType.PROPERTY,
                    "[all] members of [guild] %string%", "[guild] %string%'s members");
            Skript.registerExpression(ExprGuildOnlineMembers.class, Player.class, ExpressionType.PROPERTY,
                    "[all] online members of [guild] %string%", "[guild] %string%'s online members");

            // Register events
            Skript.registerEvent("Guild Create", SimpleEvent.class, GuildCreateEvent.class, 
                    "guild create[d]");
            Skript.registerEvent("Guild Delete", SimpleEvent.class, GuildDeleteEvent.class,
                    "guild delete[d]");
            Skript.registerEvent("Guild Join", SimpleEvent.class, GuildMemberJoinEvent.class, 
                    "guild (join|joined)", "player join[ed] guild");
            Skript.registerEvent("Guild Leave", SimpleEvent.class, GuildMemberLeaveEvent.class,
                    "guild (leave|left)", "player leave[s] guild");
            Skript.registerEvent("Guild Level Up", SimpleEvent.class, GuildLevelUpEvent.class,
                    "guild level[ed] up");
            Skript.registerEvent("Guild Rename", SimpleEvent.class, GuildRenameEvent.class,
                    "guild rename[d]");

            // Register event values
            EventValues.registerEventValue(GuildCreateEvent.class, Guild.class,
                    new Getter<Guild, GuildCreateEvent>() {
                        @Override
                        public Guild get(GuildCreateEvent event) {
                            return event.getGuild();
                        }
                    }, 0);

            EventValues.registerEventValue(GuildDeleteEvent.class, Guild.class,
                    new Getter<Guild, GuildDeleteEvent>() {
                        @Override
                        public Guild get(GuildDeleteEvent event) {
                            return event.getGuild();
                        }
                    }, 0);

            EventValues.registerEventValue(GuildMemberJoinEvent.class, Guild.class,
                    new Getter<Guild, GuildMemberJoinEvent>() {
                        @Override
                        public Guild get(GuildMemberJoinEvent event) {
                            return event.getGuild();
                        }
                    }, 0);

            EventValues.registerEventValue(GuildMemberJoinEvent.class, Player.class,
                    new Getter<Player, GuildMemberJoinEvent>() {
                        @Override
                        public Player get(GuildMemberJoinEvent event) {
                            return Bukkit.getPlayer(event.getPlayer());
                        }
                    }, 0);

            EventValues.registerEventValue(GuildMemberLeaveEvent.class, Guild.class,
                    new Getter<Guild, GuildMemberLeaveEvent>() {
                        @Override
                        public Guild get(GuildMemberLeaveEvent event) {
                            return event.getGuild();
                        }
                    }, 0);

            EventValues.registerEventValue(GuildMemberLeaveEvent.class, Player.class,
                    new Getter<Player, GuildMemberLeaveEvent>() {
                        @Override
                        public Player get(GuildMemberLeaveEvent event) {
                            return Bukkit.getPlayer(event.getPlayer());
                        }
                    }, 0);

            EventValues.registerEventValue(GuildLevelUpEvent.class, Guild.class,
                    new Getter<Guild, GuildLevelUpEvent>() {
                        @Override
                        public Guild get(GuildLevelUpEvent event) {
                            return event.getGuild();
                        }
                    }, 0);

            EventValues.registerEventValue(GuildLevelUpEvent.class, Number.class,
                    new Getter<Number, GuildLevelUpEvent>() {
                        @Override
                        public Number get(GuildLevelUpEvent event) {
                            return event.getNewLevel();
                        }
                    }, 0);

            plugin.getLogger().info("Successfully hooked into Skript!");
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into Skript: " + e.getMessage());
            plugin.getLogger().warning("Skript integration will be disabled.");
        }
    }

    private boolean isSkriptInstalled() {
        try {
            Class.forName("ch.njol.skript.Skript");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
}
