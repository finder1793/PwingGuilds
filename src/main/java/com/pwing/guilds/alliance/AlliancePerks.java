package com.pwing.guilds.alliance;

import org.bukkit.configuration.serialization.ConfigurationSerializable;
import java.util.HashMap;
import java.util.Map;

public class AlliancePerks implements ConfigurationSerializable {
    private double expBonus;
    private int extraClaims;
    private boolean sharedTeleport;

    public AlliancePerks() {
        this.expBonus = 1.0;
        this.extraClaims = 0;
        this.sharedTeleport = false;
    }

    public double getExpBonus() {
        return expBonus;
    }

    public void setExpBonus(double expBonus) {
        this.expBonus = expBonus;
    }

    public int getExtraClaims() {
        return extraClaims;
    }

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
