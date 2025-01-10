package com.pwing.guilds.alliance;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents the perks/benefits that an alliance provides to its member guilds
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
     * Gets the experience bonus multiplier for alliance members
     * @return The experience bonus multiplier
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
     * Gets the number of extra land claims available to alliance members
     * @return The number of extra claims
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
