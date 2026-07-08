package com.sprint.mission.discodeit.security.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sprint.mission.discodeit.dto.response.JwtDto;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.service.RefreshTokenService;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLoginSuccessHandler implements AuthenticationSuccessHandler {
//TODO: JwtTokenProvider, ObjectMapper, JwtRegistry(후반부에 구현)를 주입받으세요.
  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper;
  private final RefreshTokenService refreshTokenService;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    // 인증 성공한 사용자 정보 꺼내기
    DiscodeitUserDetails userDetails =
        (DiscodeitUserDetails) authentication.getPrincipal();

    // 각 토큰 발급
    String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
    String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

    refreshTokenService.saveRefreshToken(
        refreshToken,
        userDetails.getUserDto().id(),
        jwtTokenProvider.getExpiration(refreshToken)
    );

    // Refresh 토큰은 Httponly
    Cookie refreshTokenCookie = new Cookie(
        JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME,
        refreshToken
    );
    refreshTokenCookie.setHttpOnly(true);
    refreshTokenCookie.setPath("/");
    refreshTokenCookie.setSecure(request.isSecure());

    // Refresh 토큰이 만료되는 시간에 맞춰서 쿠키도 브라우저에서 없앰
    int maxAge = (int) Duration.between(
        Instant.now(),
        jwtTokenProvider.getExpiration(refreshToken)
    ).getSeconds();
    refreshTokenCookie.setMaxAge(maxAge);

    response.addCookie(refreshTokenCookie);

    response.setStatus(HttpServletResponse.SC_OK);  // 200 OK
    response.setCharacterEncoding("UTF-8");
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);

    JwtDto jwtDto = new JwtDto(accessToken);
    String responseBody = objectMapper.writeValueAsString(jwtDto);
    response.getWriter().write(responseBody);
  }
}
