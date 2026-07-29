package com.sprint.mission.discodeit.security.jwt;

import com.sprint.mission.discodeit.redis.RedisLockProvider;
import com.sprint.mission.discodeit.redis.RedisLockProvider.RedisLockAcquisitionException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequiredArgsConstructor
public class RedisJwtRegistry implements JwtRegistry {

    private static final String USER_JWT_KEY_PREFIX = "jwt:user:";
    private static final String ACCESS_TOKEN_INDEX_KEY = "jwt:access-tokens";
    private static final String REFRESH_TOKEN_INDEX_KEY = "jwt:refresh-tokens";

    private final int maxActiveJwtCount;
    private final RedisTemplate<String, Object> redisTemplate;
    private final RedisLockProvider redisLockProvider;

    @Override
    @Retryable(
            retryFor = RedisLockAcquisitionException.class,
            maxAttempts = 10,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public void registerJwtInformation(JwtInformation jwtInformation) {
        String lockKey = jwtInformation.userId().toString();
        redisLockProvider.acquireLock(lockKey);
        try {
            String userKey = getUserKey(jwtInformation.userId());
            Long size = redisTemplate.opsForList().size(userKey);

            while (size != null && size >= maxActiveJwtCount) {
                Object oldest = redisTemplate.opsForList().leftPop(userKey);
                if (oldest instanceof JwtInformation jwtInformationToRemove) {
                    removeTokenIndex(jwtInformationToRemove);
                }
                size = redisTemplate.opsForList().size(userKey);
            }

            redisTemplate.opsForList().rightPush(userKey, jwtInformation);
            setExpiration(userKey, jwtInformation.expiration());
            addTokenIndex(jwtInformation);
        } finally {
            redisLockProvider.releaseLock(lockKey);
        }
    }

    @Override
    public void invalidateJwtInformationByUserId(UUID userId) {
        String userKey = getUserKey(userId);
        getJwtInformations(userKey).forEach(this::removeTokenIndex);
        redisTemplate.delete(userKey);
    }

    @Override
    public void invalidateJwtInformationByRefreshToken(String refreshToken) {
        findUserIdByRefreshToken(refreshToken).ifPresent(userId -> {
            String userKey = getUserKey(userId);
            getJwtInformations(userKey).stream()
                    .filter(jwtInformation ->
                            jwtInformation.refreshToken().equals(refreshToken))
                    .findFirst()
                    .ifPresent(jwtInformation -> {
                        redisTemplate.opsForList().remove(
                                userKey,
                                1,
                                jwtInformation
                        );
                        removeTokenIndex(jwtInformation);
                    });
        });
    }

    @Override
    public boolean hasActiveJwtInformationByUserId(UUID userId) {
        return getJwtInformations(getUserKey(userId)).stream()
                .anyMatch(this::isActive);
    }

    @Override
    public boolean hasActiveJwtInformationByAccessToken(String accessToken) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet()
                        .isMember(ACCESS_TOKEN_INDEX_KEY, accessToken)
        );
    }

    @Override
    public boolean hasActiveJwtInformationByRefreshToken(String refreshToken) {
        return Boolean.TRUE.equals(
                redisTemplate.opsForSet()
                        .isMember(REFRESH_TOKEN_INDEX_KEY, refreshToken)
        );
    }

    @Override
    public Optional<UUID> findUserIdByRefreshToken(String refreshToken) {
        return getUserIds().stream()
                .filter(userId -> getJwtInformations(getUserKey(userId)).stream()
                        .anyMatch(jwtInformation ->
                                jwtInformation.refreshToken().equals(refreshToken)
                                        && isActive(jwtInformation)))
                .findFirst();
    }

    @Override
    public Set<UUID> getActiveUserIds() {
        Set<String> keys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");
        if (keys == null) {
            return Set.of();
        }

        return keys.stream()
                .map(key -> UUID.fromString(
                        key.substring(USER_JWT_KEY_PREFIX.length())))
                .filter(this::hasActiveJwtInformationByUserId)
                .collect(Collectors.toSet());
    }

    @Override
    @Retryable(
            retryFor = RedisLockAcquisitionException.class,
            maxAttempts = 10,
            backoff = @Backoff(delay = 100, multiplier = 2)
    )
    public JwtInformation rotateJwtInformation(
            String oldRefreshToken,
            JwtInformation newJwtInformation
    ) {
        UUID userId = findUserIdByRefreshToken(oldRefreshToken)
                .orElseThrow(() ->
                        new IllegalArgumentException("등록되지 않은 리프레시 토큰입니다."));
        String lockKey = userId.toString();

        redisLockProvider.acquireLock(lockKey);
        try {
            String userKey = getUserKey(userId);
            JwtInformation oldJwtInformation = getJwtInformations(userKey).stream()
                    .filter(jwtInformation ->
                            jwtInformation.refreshToken().equals(oldRefreshToken))
                    .findFirst()
                    .orElseThrow(() ->
                            new IllegalArgumentException(
                                    "등록되지 않은 리프레시 토큰입니다."));

            redisTemplate.opsForList().remove(userKey, 1, oldJwtInformation);
            removeTokenIndex(oldJwtInformation);
            redisTemplate.opsForList().rightPush(userKey, newJwtInformation);
            setExpiration(userKey, newJwtInformation.expiration());
            addTokenIndex(newJwtInformation);
            return newJwtInformation;
        } finally {
            redisLockProvider.releaseLock(lockKey);
        }
    }

    @Override
    @Scheduled(fixedDelay = 1000 * 60 * 5)
    public void clearExpiredJwtInformation() {
        Set<UUID> userIds = getUserIds();
        for (UUID userId : userIds) {
            String userKey = getUserKey(userId);
            getJwtInformations(userKey).stream()
                    .filter(jwtInformation -> !isActive(jwtInformation))
                    .forEach(jwtInformation -> {
                        redisTemplate.opsForList().remove(
                                userKey,
                                1,
                                jwtInformation
                        );
                        removeTokenIndex(jwtInformation);
                    });
        }
    }

    private List<JwtInformation> getJwtInformations(String userKey) {
        List<Object> values = redisTemplate.opsForList().range(userKey, 0, -1);
        if (values == null) {
            return List.of();
        }

        return values.stream()
                .filter(JwtInformation.class::isInstance)
                .map(JwtInformation.class::cast)
                .toList();
    }

    private void addTokenIndex(JwtInformation jwtInformation) {
        Duration expiration = Duration.between(
                Instant.now(),
                jwtInformation.expiration()
        );
        redisTemplate.opsForSet().add(
                ACCESS_TOKEN_INDEX_KEY,
                jwtInformation.accessToken()
        );
        redisTemplate.opsForSet().add(
                REFRESH_TOKEN_INDEX_KEY,
                jwtInformation.refreshToken()
        );
        redisTemplate.expire(ACCESS_TOKEN_INDEX_KEY, expiration);
        redisTemplate.expire(REFRESH_TOKEN_INDEX_KEY, expiration);
    }

    private void removeTokenIndex(JwtInformation jwtInformation) {
        redisTemplate.opsForSet().remove(
                ACCESS_TOKEN_INDEX_KEY,
                jwtInformation.accessToken()
        );
        redisTemplate.opsForSet().remove(
                REFRESH_TOKEN_INDEX_KEY,
                jwtInformation.refreshToken()
        );
    }

    private Set<UUID> getUserIds() {
        Set<String> keys = redisTemplate.keys(USER_JWT_KEY_PREFIX + "*");
        if (keys == null) {
            return Set.of();
        }
        return keys.stream()
                .map(key -> UUID.fromString(
                        key.substring(USER_JWT_KEY_PREFIX.length())))
                .collect(Collectors.toSet());
    }

    private String getUserKey(UUID userId) {
        return USER_JWT_KEY_PREFIX + userId;
    }

    private void setExpiration(String key, Instant expiration) {
        redisTemplate.expire(
                key,
                Duration.between(Instant.now(), expiration)
        );
    }

    private boolean isActive(JwtInformation jwtInformation) {
        return jwtInformation.expiration().isAfter(Instant.now());
    }
}
