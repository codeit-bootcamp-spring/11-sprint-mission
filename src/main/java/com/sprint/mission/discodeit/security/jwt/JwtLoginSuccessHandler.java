package com.sprint.mission.discodeit.security.jwt;

import static jakarta.servlet.http.HttpServletResponse.SC_OK;
import static org.springframework.http.HttpHeaders.SET_COOKIE;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.auth.JwtResponse;
import com.sprint.mission.discodeit.dto.user.UserResponse;
import com.sprint.mission.discodeit.exception.ErrorResponse;
import com.sprint.mission.discodeit.exception.auth.InvalidUserDetailsException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final JwtRegistry jwtRegistry;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {
    response.setCharacterEncoding("UTF-8");
    response.setContentType(APPLICATION_JSON_VALUE);

    if (!(authentication.getPrincipal() instanceof DiscodeitUserDetails userDetails)) {
      log.error("auth login failed: invalid principal type={}",
          authentication.getPrincipal().getClass().getSimpleName());
      response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
      ErrorResponse errorResponse = ErrorResponse.from(
          InvalidUserDetailsException.withInvalidPrincipal()
      );
      response.getWriter().write(objectMapper.writeValueAsString(errorResponse));
      return;
    }

    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
    Instant expiration = jwtTokenProvider.getExpiration(refreshToken);

    ResponseCookie cookie = ResponseCookie
        .from(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .sameSite("Lax")
        .path("/")
        .maxAge(Duration.between(Instant.now(), expiration))
        .build();
    response.addHeader(SET_COOKIE, cookie.toString());

    response.setStatus(SC_OK);
    UserResponse userResponse = userDetails.getUser();
    JwtResponse jwtResponse = new JwtResponse(userResponse, accessToken);
    response.getWriter().write(objectMapper.writeValueAsString(jwtResponse));

    jwtRegistry.registerJwtInformation(
        new JwtInformation(userResponse, accessToken, refreshToken, expiration)
    );
    log.info("auth login success: userId={}, username={}", userResponse.id(),
        userResponse.username());
  }
}
