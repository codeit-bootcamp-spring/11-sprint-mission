package com.sprint.mission.discodeit.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.sprint.mission.discodeit.security.DiscodeitUserDetails;
import com.sprint.mission.discodeit.security.JwtDto;
import com.sprint.mission.discodeit.security.jwt.JwtTokenProvider;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
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
  // private final JwtRegistry jwtRegistry;

  @Override
  public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
      Authentication authentication) throws IOException, ServletException {

    DiscodeitUserDetails userDetails = (DiscodeitUserDetails) authentication.getPrincipal();

    try {
      String accessToken = jwtTokenProvider.generateAccessToken(userDetails);
      String refreshToken = jwtTokenProvider.generateRefreshToken(userDetails);

      ResponseCookie responseCookie = ResponseCookie.from(
              JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME, refreshToken)
          .httpOnly(true)
          .path("/api/auth")
          //.secure(true)
          .maxAge(jwtTokenProvider.getRefreshTokenExpiration() / 1000)
          .build();

      response.addHeader("Set-Cookie", responseCookie.toString());
      response.setStatus(HttpServletResponse.SC_OK);
      response.setContentType(MediaType.APPLICATION_JSON_VALUE);
      response.setCharacterEncoding("UTF-8");

      JwtDto jwtDto = new JwtDto(userDetails.getUserDto(), accessToken);
      response.getWriter().write(objectMapper.writeValueAsString(jwtDto));
    } catch (JOSEException e) {
      // TODO: 커스텀 예외 작성 (토큰 서명 시크릿 키 길이가 짧거나 서버 암호화 모듈이 고장 난 '서버측 인프라 에러(500)' 이다.
      throw new RuntimeException("토큰 발급 중 오류가 발생했습니다.");
    }
  }
}
