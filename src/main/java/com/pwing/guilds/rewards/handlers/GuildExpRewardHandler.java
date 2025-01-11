package com.pwing.guilds.rewards.handlers;

import com.pwing.guilds.rewards.RewardHandler;
import com.pwing.guilds.guild.Guild;

public class GuildExpRewardHandler implements RewardHandler {
    @Override
    public boolean handleReward(Guild guild, String reward) {
        if (!reward.startsWith("guild-exp")) {
            return false;
        }

        try {
            int amount = Integer.parseInt(reward.split(" ")[1]);
            guild.addExp(amount);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
