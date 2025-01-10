package com.pwing.guilds.alliance;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents bonuses and benefits provided to guilds in an alliance.
 * Includes experience multipliers, extra claim slots, and shared features.
 * Perks are configurable and can be enabled/disabled per alliance.
 */
public class AlliancePerks implements ConfigurationSerializable {
    private double expBonus;
    private int extraClaims;
    private boolean sharedTeleport;

    /**
     * Creates a new AlliancePerks instance with default values
     */
    public AlliancePerks() {
        this.expBonus = 1.0;
        this.extraClaims = 0;
        this.sharedTeleport = false;
    }

    /**
     * Gets the experience multiplier for alliance members
     * Values above 1.0 provide bonus exp, below 1.0 reduce exp gain
     * @return The exp multiplier value
     */
    public double getExpBonus() {
        return expBonus;
    }

    /**
     * Sets the experience bonus multiplier
     * @param expBonus The new experience bonus multiplier
     */
    public void setExpBonus(double expBonus) {
        this.expBonus = expBonus;
    }

    /**
     * Gets the number of additional chunks that can be claimed
     * This is added to the guild's base claim limit
     * @return Number of extra chunk claims
     */
    public int getExtraClaims() {
        return extraClaims;
    }

    /**
     * Sets the number of extra land claims
     * @param extraClaims The new number of extra claims
     */
    public void setExtraClaims(int extraClaims) {
        this.extraClaims = extraClaims;
    }

    @Override
    public Map<String, Object> serialize() {
        Map<String, Object> data = new HashMap<>();
        data.put("exp-bonus", expBonus);
        data.put("extra-claims", extraClaims);
        data.put("shared-teleport", sharedTeleport);
        return data;
    }
}
