package com.sprint.mission.discodeit.security.jwt;

import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class InMemoryJwtRegistry implements JwtRegistry {

    private final Map<UUID, Queue<JwtInformation>> origin = new ConcurrentHashMap<>();
    private final int maxActiveJwtCount;

    public InMemoryJwtRegistry(int maxActiveJwtCount) {
        this.maxActiveJwtCount = maxActiveJwtCount;
    }

    @Override
    public void registerJwtInformation(JwtInformation jwtInformation) {
        origin.compute(jwtInformation.userId(), (userId, queue) -> {
            Queue<JwtInformation> jwtInformations = queue == null
                    ? new ConcurrentLinkedQueue<>()
                    : queue;

            jwtInformations.offer(jwtInformation);
            while (jwtInformations.size() > maxActiveJwtCount) {
                jwtInformations.poll();
            }
            return jwtInformations;
        });
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        origin.remove(userId);
    }

    @Override
    public void invalidateJwtInformationByRefreshToken(String refreshToken) {
        origin.forEach((userId, queue) -> {
            queue.removeIf(jwtInformation ->
                    jwtInformation.refreshToken().equals(refreshToken));
            if (queue.isEmpty()) {
                origin.remove(userId, queue);
            }
        });
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        Queue<JwtInformation> jwtInformations = origin.get(userId);
        return jwtInformations != null
                && jwtInformations.stream().anyMatch(this::isActive);
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(jwtInformation -> isActive(jwtInformation)
                        && jwtInformation.accessToken().equals(accessToken));
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return origin.values().stream()
                .flatMap(Queue::stream)
                .anyMatch(jwtInformation -> isActive(jwtInformation)
                        && jwtInformation.refreshToken().equals(refreshToken));
    }

    @Override
    public Set<UUID> getActiveUserIds() {
        return origin.entrySet().stream()
                .filter(entry -> entry.getValue().stream().anyMatch(this::isActive))
                .map(Map.Entry::getKey)
                .collect(Collectors.toSet());
    }

    @Override
    public JwtInformation rotateJwtInformation(
            String oldRefreshToken,
            JwtInformation newJwtInformation
    ) {
        AtomicBoolean removed = new AtomicBoolean(false);
        origin.forEach((userId, queue) -> {
            boolean removedFromQueue = queue.removeIf(jwtInformation ->
                    jwtInformation.refreshToken().equals(oldRefreshToken));
            removed.compareAndSet(false, removedFromQueue);
            if (queue.isEmpty()) {
                origin.remove(userId, queue);
            }
        });

        if (!removed.get()) {
            throw new IllegalArgumentException("등록되지 않은 리프레시 토큰입니다.");
        }

        registerJwtInformation(newJwtInformation);
        return newJwtInformation;
    }

    @Scheduled(fixedDelay = 1000 * 60 * 5)
    @Override
    public void clearExpiredJwtInformation() {
        Instant now = Instant.now();
        origin.forEach((userId, queue) -> {
            queue.removeIf(jwtInformation ->
                    jwtInformation.expiration().isBefore(now));
            if (queue.isEmpty()) {
                origin.remove(userId, queue);
            }
        });
    }

    private boolean isActive(JwtInformation jwtInformation) {
        return jwtInformation.expiration().isAfter(Instant.now());
    }
}
