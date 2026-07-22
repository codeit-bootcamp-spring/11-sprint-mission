package com.sprint.mission.discodeit.security.handler;

import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
        Authentication authentication) {

        if (request.getCookies() == null) {
            return;
        }

        Arrays.stream(request.getCookies())
            .filter(cookie -> cookie.getName().equals(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME))
            .findFirst()
            .ifPresent(cookie -> {
                try {
                    String userId = jwtTokenProvider.getSubject(cookie.getValue());
                    jwtRegistry.invalidateJwtInformationByUserId(UUID.fromString(userId));
                } catch (Exception ignored) {
                    // 리프레시 토큰이 만료/손상되어도 쿠키 삭제는 진행
                }

                ResponseCookie expiredCookie = ResponseCookie
                    .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, "")
                    .httpOnly(true)
                    .path("/")
                    .sameSite("Strict")
                    .maxAge(0)
                    .build();
                response.addHeader(HttpHeaders.SET_COOKIE, expiredCookie.toString());
            });
    }
}
