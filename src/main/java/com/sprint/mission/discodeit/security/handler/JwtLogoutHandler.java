package com.sprint.mission.discodeit.security.handler;

import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtRegistry jwtRegistry;
    private final CacheManager cacheManager;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            Arrays.stream(cookies)
                    .filter(cookie -> cookie.getName().equals(
                            JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
                    .findFirst()
                    .ifPresent(cookie ->
                            jwtRegistry.invalidateJwtInformationByRefreshToken(
                                    cookie.getValue()));
        }

        Cookie expiredCookie = new Cookie(
                JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                ""
        );
        expiredCookie.setHttpOnly(true);
        expiredCookie.setSecure(request.isSecure());
        expiredCookie.setPath("/");
        expiredCookie.setMaxAge(0);
        response.addCookie(expiredCookie);
        evictUsersCache();
    }

    private void evictUsersCache() {
        Cache cache = cacheManager.getCache("users");
        if (cache != null) {
            cache.clear();
        }
    }
}
