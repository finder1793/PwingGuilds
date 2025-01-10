package com.pwing.guilds.alliance.storage;

import com.pwing.guilds.alliance.Alliance;
import java.util.Set;

/**
 * Interface for alliance data storage operations
 */
public interface AllianceStorage {
    /**
     * Saves an alliance to storage
     * @param alliance The alliance to save
     */
    void saveAlliance(Alliance alliance);

    /**
     * Loads an alliance from storage
     * @param name The name of the alliance to load
     * @return The loaded Alliance instance
     */
    Alliance loadAlliance(String name);

    /**
     * Deletes an alliance from storage
     * @param name The name of the alliance to delete
     */
    void deleteAlliance(String name);

    /**
     * Loads all alliances from storage
     * @return Set of all stored alliances
     */
    Set<Alliance> loadAllAlliances();
}
