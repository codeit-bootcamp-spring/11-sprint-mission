package com.sprint.mission.discodeit.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.JwtDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.jwt.JwtInformation;
import com.sprint.mission.discodeit.security.jwt.JwtRegistry;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;
  private final JwtRegistry jwtRegistry;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();

    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    Instant expiration = jwtTokenProvider.getExpiration(refreshToken);
    JwtInformation jwtInformation = new JwtInformation(userDetails.getUserDto().id(), accessToken,
        refreshToken, expiration);

    jwtRegistry.invalidateJwtInformationByUserId(userDetails.getUserDto().id());
    jwtRegistry.registerJwtInformation(jwtInformation);

    ResponseCookie refreshTokenCookie = ResponseCookie.from(
            JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
        .httpOnly(true)
        .path("/")
        .secure(true)
        .sameSite("Strict")
        .build();
    response.addHeader(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString());

    response.setStatus(HttpStatus.OK.value());
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    JwtDto jwtDto = new JwtDto(accessToken, userDetails.getUserDto());
    objectMapper.writeValue(response.getWriter(), jwtDto);
  }
}