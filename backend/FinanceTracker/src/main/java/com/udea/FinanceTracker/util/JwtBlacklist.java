package com.udea.FinanceTracker.util;

import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * JWT Blacklist Component
 * Stores invalidated tokens to prevent them from being used after account deletion
 * This is a simple in-memory solution. For production, consider using Redis or a database.
 */
@Component
public class JwtBlacklist {

    // Thread-safe set to store blacklisted tokens
    private final Set<String> blacklistedTokens = ConcurrentHashMap.newKeySet();

    /**
     * Add a token to the blacklist
     * @param token The JWT token to blacklist
     */
    public void blacklistToken(String token) {
        blacklistedTokens.add(token);
    }

    /**
     * Check if a token is blacklisted
     * @param token The JWT token to check
     * @return true if token is blacklisted, false otherwise
     */
    public boolean isBlacklisted(String token) {
        return blacklistedTokens.contains(token);
    }

    /**
     * Remove a token from the blacklist (optional, for cleanup)
     * @param token The JWT token to remove
     */
    public void removeFromBlacklist(String token) {
        blacklistedTokens.remove(token);
    }

    /**
     * Clear all blacklisted tokens (for testing or maintenance)
     */
    public void clearBlacklist() {
        blacklistedTokens.clear();
    }

    /**
     * Get the size of the blacklist
     * @return number of blacklisted tokens
     */
    public int getBlacklistSize() {
        return blacklistedTokens.size();
    }
}

