package com.pwing.guilds.alliance.storage;

import com.pwing.guilds.alliance.Alliance;
import java.util.Set;

public interface AllianceStorage {
    void saveAlliance(Alliance alliance);
    Alliance loadAlliance(String name);
    void deleteAlliance(String name);
    Set<Alliance> loadAllAlliances();
}
