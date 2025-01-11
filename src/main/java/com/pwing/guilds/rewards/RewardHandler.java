package com.pwing.guilds.rewards;

import com.pwing.guilds.guild.Guild;

public interface RewardHandler {
    boolean handleReward(Guild guild, String reward);
}
