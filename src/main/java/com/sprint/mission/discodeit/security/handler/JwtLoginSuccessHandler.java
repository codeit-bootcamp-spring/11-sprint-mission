package com.sprint.mission.discodeit.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;
    private final ObjectMapper objectMapper;
    private final CacheManager cacheManager;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException, ServletException {
        DiscodeitUserDetails userDetails =
                (DiscodeitUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

        JwtInformation jwtInformation = new JwtInformation(
                userDetails.getUserDto().id(),
                accessToken,
                refreshToken,
                jwtTokenProvider.getExpiration(refreshToken)
        );
        jwtRegistry.registerJwtInformation(jwtInformation);
        evictUsersCache();

        Cookie refreshTokenCookie = new Cookie(
                JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                refreshToken
        );
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setSecure(request.isSecure());
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge((int) Duration.between(
                java.time.Instant.now(),
                jwtInformation.expiration()
        ).toSeconds());
        response.addCookie(refreshTokenCookie);

        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());

        objectMapper.writeValue(
                response.getWriter(),
                new JwtDto(accessToken, userDetails.getUserDto())
        );
    }

    private void evictUsersCache() {
        Cache cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.clear();
        }
    }
}
