package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

  private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();
  private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

  @Override
  public void handle(HttpServletRequest request, HttpServletResponse response,
      Supplier<CsrfToken> csrfToken) {
    // 응답 렌더링 시 CSRF 토큰 BREACH 보호 적용함
    this.xor.handle(request, response, csrfToken);

    // 지연 로딩된 CSRF 토큰을 강제로 읽어서 쿠키에 렌더링되게 함
    csrfToken.get();
  }

  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    // SPA는 쿠키에서 읽은 원본 토큰 값을 X-XSRF-TOKEN 헤더로 전송함
    String headerValue = request.getHeader(csrfToken.getHeaderName());

    // 헤더가 있으면 원본 토큰 방식으로 검증함
    // 헤더가 없으면 기본 XOR 방식으로 검증함
    return StringUtils.hasText(headerValue)
        ? this.plain.resolveCsrfTokenValue(request, csrfToken)
        : this.xor.resolveCsrfTokenValue(request, csrfToken);
  }
}