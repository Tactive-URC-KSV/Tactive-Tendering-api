package com.ksv.tactive.service;

import jakarta.servlet.http.HttpServletRequest;

public interface LoginRateLimiterService {
    /** Returns true if the given IP or username is currently locked out. */
    boolean isBlocked(String ip, String username);

    /** Records one failed attempt for both IP and username. */
    void recordFailedAttempt(String ip, String username);

    /** Clears the failure counters on a successful login. */
    void resetAttempts(String ip, String username);

    /** How many attempts remain before lockout (0 means locked). */
    int getRemainingAttempts(String ip, String username);

    /** Minutes remaining in the current lockout window. */
    long getRemainingLockoutMinutes(String ip, String username);

    /** Resolves the real client IP, respecting X-Forwarded-For. */
    String getClientIp(HttpServletRequest request);
}
