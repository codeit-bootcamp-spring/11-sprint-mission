package com.sprint.mission.discodeit.security.handler;

import static com.sprint.mission.discodeit.security.jwt.provider.JwtTokenProvider.REFRESH_TOKEN_COOKIE_NAME;

import com.sprint.mission.discodeit.security.jwt.registry.JwtRegistry;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtLogoutHandler implements LogoutHandler {

  private final JwtRegistry jwtRegistry;

  @Override
  public void logout(
      HttpServletRequest request,
      HttpServletResponse response,
      Authentication authentication
  ) {

    // 쿠키가 존재하지 않으면 건너뜀
    if (request.getCookies() == null) {
      return;
    }

    // ===== 서버(JVM 메모리)에서 RefreshToken을 무효화 ===== //
    // Cookie들을 stream()으로 흘림
    // REFRESH_TOKEN_COOKIE_NAME(REFRESH_TOKEN)에 맞는 쿠키 중 첫 번째 쿠키를 가져옴(동시 로그인 제한이기 때문에 사실상 1개)
    // REFRESH_TOKEN 쿠키에는 Refresh Token 값만 있고 .getValue()를 통해 Refresh Token을 가져옴
    // 해당 Refresh Token을 무효화시킴
    Arrays.stream(request.getCookies())
        .filter(cookie -> cookie.getName().equals(REFRESH_TOKEN_COOKIE_NAME))
        .findFirst()
        .ifPresent(cookie -> {
          String refreshToken = cookie.getValue();
          jwtRegistry.invalidateJwtInformationByRefreshToken(refreshToken);
        });

    // ===== 브라우저(클라이언트) 쿠키 삭제 ===== //
    // 쿠키 생성
    Cookie cookie = new Cookie(REFRESH_TOKEN_COOKIE_NAME, null);

    // JavaScript 차단, 내 도메인 내의 모든 URI에 쿠키 적용
    cookie.setHttpOnly(true);
    cookie.setPath("/");

    // 즉시 만료되도록 유도
    cookie.setMaxAge(0);

    // 브라우저 쿠키 갱신(즉시 만료된 쿠키로 갱신되기 때문에 삭제 개념)
    response.addCookie(cookie);
  }
}
