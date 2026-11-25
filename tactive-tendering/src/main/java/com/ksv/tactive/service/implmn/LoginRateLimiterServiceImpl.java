package com.ksv.tactive.service.implmn;

import com.ksv.tactive.service.LoginRateLimiterService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoginRateLimiterServiceImpl implements LoginRateLimiterService {

    @Value("${security.rate-limit.login.max-attempts:5}")
    private int maxAttempts;

    @Value("${security.rate-limit.login.lockout-duration-minutes:2}")
    private long lockoutDurationMinutes;

    private static class AttemptRecord {
        int attempts = 0;
        long lockedUntilEpochMs = 0;
        long lastAttemptEpochMs = 0;
    }

    private final Map<String, AttemptRecord> attemptCache = new ConcurrentHashMap<>();

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    @Override
    public boolean isBlocked(String ip, String username) {
        long now = System.currentTimeMillis();
        return isKeyBlocked(ipKey(ip), now) || isKeyBlocked(userKey(username), now);
    }

    @Override
    public void recordFailedAttempt(String ip, String username) {
        long now = System.currentTimeMillis();
        long lockoutMillis = lockoutDurationMinutes * 60_000L;
        recordKeyAttempt(ipKey(ip), now, lockoutMillis);
        recordKeyAttempt(userKey(username), now, lockoutMillis);
    }

    @Override
    public void resetAttempts(String ip, String username) {
        String ik = ipKey(ip);
        String uk = userKey(username);
        if (ik != null) attemptCache.remove(ik);
        if (uk != null) attemptCache.remove(uk);
    }

    @Override
    public int getRemainingAttempts(String ip, String username) {
        int ipAttempts   = getAttempts(ipKey(ip));
        int userAttempts = getAttempts(userKey(username));
        int used         = Math.max(ipAttempts, userAttempts);
        return Math.max(maxAttempts - used, 0);
    }

    @Override
    public long getRemainingLockoutMinutes(String ip, String username) {
        long now = System.currentTimeMillis();
        long maxMs = Math.max(getRemainingMs(ipKey(ip), now), getRemainingMs(userKey(username), now));
        if (maxMs <= 0) return 0;
        return (long) Math.ceil(maxMs / 60_000.0);
    }

    @Override
    public String getClientIp(HttpServletRequest request) {
        if (request == null) return "unknown";
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank() && !"unknown".equalsIgnoreCase(xff))
            return xff.split(",")[0].trim();
        String xri = request.getHeader("X-Real-IP");
        if (xri != null && !xri.isBlank() && !"unknown".equalsIgnoreCase(xri))
            return xri.trim();
        String remote = request.getRemoteAddr();
        return remote != null ? remote.trim() : "unknown";
    }

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private String ipKey(String ip) {
        return (ip != null && !ip.isBlank()) ? "ip:" + ip.trim() : null;
    }

    private String userKey(String username) {
        return (username != null && !username.isBlank()) ? "user:" + username.trim().toLowerCase() : null;
    }

    private boolean isKeyBlocked(String key, long now) {
        if (key == null) return false;
        AttemptRecord r = attemptCache.get(key);
        if (r == null) return false;
        if (r.lockedUntilEpochMs > now) return true;
        // Lockout has expired — purge stale entry
        if (r.lockedUntilEpochMs > 0) attemptCache.remove(key);
        return false;
    }

    private void recordKeyAttempt(String key, long now, long lockoutMillis) {
        if (key == null) return;
        attemptCache.compute(key, (k, r) -> {
            if (r == null) r = new AttemptRecord();
            // If a previous lockout has expired, or the last attempt is older
            // than the lockout window, start fresh
            boolean lockoutExpired  = r.lockedUntilEpochMs > 0 && r.lockedUntilEpochMs <= now;
            boolean windowExpired   = r.lastAttemptEpochMs > 0 && (now - r.lastAttemptEpochMs > lockoutMillis);
            if (lockoutExpired || windowExpired) {
                r = new AttemptRecord();
            }
            r.attempts++;
            r.lastAttemptEpochMs = now;
            if (r.attempts >= maxAttempts) {
                r.lockedUntilEpochMs = now + lockoutMillis;
            }
            return r;
        });
    }

    private int getAttempts(String key) {
        if (key == null) return 0;
        AttemptRecord r = attemptCache.get(key);
        return r != null ? r.attempts : 0;
    }

    private long getRemainingMs(String key, long now) {
        if (key == null) return 0;
        AttemptRecord r = attemptCache.get(key);
        if (r == null || r.lockedUntilEpochMs <= now) return 0;
        return r.lockedUntilEpochMs - now;
    }
}
