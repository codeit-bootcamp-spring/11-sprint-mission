package com.sprint.mission.discodeit.security.jwt;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class JwtRegistry {

    private final Map<String, String> refreshTokenByUsername = new ConcurrentHashMap<>();

    public void register(String username, String refreshToken) {
        refreshTokenByUsername.put(username, refreshToken);
    }

    public boolean isValidRefreshToken(String username, String refreshToken) {
        return refreshToken.equals(refreshTokenByUsername.get(username));
    }
}
