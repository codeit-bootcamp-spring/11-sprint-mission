package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException {
    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();

    String userId = userDetails.getUserDto().id().toString();
    String username = userDetails.getUsername();
    String role = userDetails.getAuthorities().iterator().next().getAuthority();

    String accessToken = jwtTokenProvider.generateAccessToken(
        userDetails.getUserDto().id(), username, role);
    String refreshToken = jwtTokenProvider.generateRefreshToken(
        userDetails.getUserDto().id(), username, role);

    // 리프레시 토큰 → 쿠키
    Cookie refreshCookie = new Cookie(JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken);
    refreshCookie.setHttpOnly(true);
    refreshCookie.setPath("/");
    refreshCookie.setMaxAge((int) (jwtTokenProvider.getRefreshTokenExpiration() / 1000));
    response.addCookie(refreshCookie);

    // 액세스 토큰 → 응답 Body
    response.setStatus(HttpServletResponse.SC_OK);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");
    objectMapper.writeValue(response.getWriter(), JwtDto.of(accessToken));
  }
}