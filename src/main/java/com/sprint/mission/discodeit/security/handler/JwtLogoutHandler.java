package com.sprint.mission.discodeit.security.handler;

import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.event.UserChangedEvent;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

    private final JwtRegistry jwtRegistry;
    private final DiscodeitUserDetailsService userDetailsService;
    private final ApplicationEventPublisher eventPublisher;

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
                    .ifPresent(cookie -> logout(cookie.getValue()));
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
    }

    private void logout(String refreshToken) {
        Optional<UUID> userId =
                jwtRegistry.findUserIdByRefreshToken(refreshToken);
        jwtRegistry.invalidateJwtInformationByRefreshToken(refreshToken);

        userId.ifPresent(id -> {
            DiscodeitUserDetails userDetails = userDetailsService.loadUserById(id);
            UserDto userDto = userDetails.getUserDto();
            eventPublisher.publishEvent(new UserChangedEvent(
                    "users.updated",
                    new UserDto(
                            userDto.id(),
                            userDto.username(),
                            userDto.email(),
                            userDto.profile(),
                            false,
                            userDto.role()
                    )
            ));
        });
    }
}
