package com.sprint.mission.discodeit.controller;

import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.dto.UserDto;
import com.sprint.mission.discodeit.dto.UserRoleUpdateRequest;
import com.sprint.mission.discodeit.exception.auth.RefreshTokenInvalidException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.DiscodeitUserDetailsService;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import com.sprint.mission.discodeit.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final DiscodeitUserDetailsService userDetailsService;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtRegistry jwtRegistry;

    public AuthController(
            UserService userService,
            DiscodeitUserDetailsService userDetailsService,
            JwtTokenProvider jwtTokenProvider,
            JwtRegistry jwtRegistry
    ) {
        this.userService = userService;
        this.userDetailsService = userDetailsService;
        this.jwtTokenProvider = jwtTokenProvider;
        this.jwtRegistry = jwtRegistry;
    }

    @GetMapping("/csrf-token")
    public ResponseEntity<Void> getCsrfToken(CsrfToken csrfToken) {
        String tokenValue = csrfToken.getToken();
        log.debug("CSRF 토큰 요청: {}", tokenValue);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtDto> refresh(
            @CookieValue(
                    name = JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                    required = false
            ) String refreshToken,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (refreshToken == null
                || !jwtTokenProvider.validateToken(refreshToken)
                || !jwtRegistry.hasActiveJwtInformationByRefreshToken(refreshToken)) {
            throw new RefreshTokenInvalidException();
        }

        try {
            UUID userId = UUID.fromString(jwtTokenProvider.getSubject(refreshToken));
            DiscodeitUserDetails userDetails = userDetailsService.loadUserById(userId);
            String newAccessToken = jwtTokenProvider.reissueAccessToken(refreshToken);
            String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

            JwtInformation newJwtInformation = new JwtInformation(
                    userId,
                    newAccessToken,
                    newRefreshToken,
                    jwtTokenProvider.getExpiration(newRefreshToken)
            );
            jwtRegistry.rotateJwtInformation(refreshToken, newJwtInformation);

            Cookie refreshTokenCookie = new Cookie(
                    JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
                    newRefreshToken
            );
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(request.isSecure());
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge((int) Duration.between(
                    Instant.now(),
                    newJwtInformation.expiration()
            ).toSeconds());
            response.addCookie(refreshTokenCookie);

            return ResponseEntity.ok(
                    new JwtDto(newAccessToken, userDetails.getUserDto())
            );
        } catch (IllegalArgumentException | UsernameNotFoundException e) {
            throw new RefreshTokenInvalidException();
        }
    }

    @PutMapping("/role")
    public ResponseEntity<UserDto> updateRole(
            @RequestBody UserRoleUpdateRequest request) {
        return ResponseEntity.ok(userService.updateRole(request));
    }
}
