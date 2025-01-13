package com.pwing.guilds.rewards;

import com.pwing.guilds.guild.Guild;

/**
 * Interface for handling rewards in the PwingGuilds plugin.
 */
public interface RewardHandler {

    /**
     * Handles a reward for a guild.
     * @param guild The guild receiving the reward
     * @param reward The reward to handle
     * @return true if the reward was handled successfully, false otherwise
     */
    boolean handleReward(Guild guild, String reward);
}
