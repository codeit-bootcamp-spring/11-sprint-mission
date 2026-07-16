package com.sprint.mission.discodeit.security.jwt;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryJwtRegistry implements JwtRegistry {

    private final Map<UUID, Deque<JwtInformation>> origin = new ConcurrentHashMap<>();
    private final int maxActiveJwtCount;

    public InMemoryJwtRegistry(int maxActiveJwtCount) {
        this.maxActiveJwtCount = maxActiveJwtCount;
    }

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        Deque<JwtInformation> deque = origin.computeIfAbsent(
            jwtInformation.userId(), k -> new ArrayDeque<>());
        synchronized (deque) {
            deque.addLast(jwtInformation);
            while (deque.size() > maxActiveJwtCount) {
                deque.pollFirst(); // 가장 오래된 세션 제거
            }
        }
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.remove(userId);
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Deque<JwtInformation> deque = origin.get(userId);
        if (deque == null) {
            return false;
        }
        synchronized (deque) {
            return !deque.isEmpty();
        }
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        for (Deque<JwtInformation> deque : origin.values()) {
            synchronized (deque) {
                for (JwtInformation info : deque) {
                    if (info.accessToken().equals(accessToken)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        for (Deque<JwtInformation> deque : origin.values()) {
            synchronized (deque) {
                for (JwtInformation info : deque) {
                    if (info.refreshToken().equals(refreshToken)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public JwtInformation rotateJwtInformation(String oldRefreshToken, JwtInformation newJwtInformation) {
        Deque<JwtInformation> deque = origin.computeIfAbsent(
            newJwtInformation.userId(), k -> new ArrayDeque<>());
        synchronized (deque) {
            deque.removeIf(info -> info.refreshToken().equals(oldRefreshToken));
            deque.addLast(newJwtInformation);
            while (deque.size() > maxActiveJwtCount) {
                deque.pollFirst();
            }
        }
        return newJwtInformation;
    }

    @Override
    public void clearExpiredJwtInformation() {
        Instant now = Instant.now();
        origin.forEach((userId, deque) -> {
            synchronized (deque) {
                deque.removeIf(info -> !info.expiration().isAfter(now));
            }
        });
        origin.entrySet().removeIf(entry -> {
            synchronized (entry.getValue()) {
                return entry.getValue().isEmpty();
            }
        });
    }
}
