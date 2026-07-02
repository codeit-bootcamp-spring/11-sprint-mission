package com.sprint.mission.discodeit.security.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.function.Supplier;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;
import org.springframework.security.web.csrf.CsrfTokenRequestHandler;
import org.springframework.security.web.csrf.XorCsrfTokenRequestAttributeHandler;
import org.springframework.util.StringUtils;

public class SpaCsrfTokenRequestHandler implements CsrfTokenRequestHandler {

  // 일반적인 CSRF 처리 핸들러(헤더값을 그대로 사용)
  private final CsrfTokenRequestHandler plain = new CsrfTokenRequestAttributeHandler();

  // XOR 방식의 CSRF 처리 핸들러(헤더값을 XOR 방식으로 변환시켜 BREACH 공격을 방어)
  // 예를 들어 헤더 값이 "abc"이면 a, b, c는 유니코드 값(97, 98, 99)를
  // 이진수로 변환시켜(01100001, 01100010, 01100011) 배열로 만들고 랜덤한 마스크 값을 넣어 XOR 변환
  private final CsrfTokenRequestHandler xor = new XorCsrfTokenRequestAttributeHandler();

  @Override
  public void handle(
      HttpServletRequest request,
      HttpServletResponse response,
      Supplier<CsrfToken> csrfToken
  ) {
    // XOR 기법으로 CSRF Token을 보호
    this.xor.handle(request, response, csrfToken);

    // Lazy Loading 방지를 위해 명시
    csrfToken.get();
  }

  @Override
  public String resolveCsrfTokenValue(HttpServletRequest request, CsrfToken csrfToken) {
    // CSRF Token 헤더
    String headerValue = request.getHeader(csrfToken.getHeaderName());

    // 헤더 값이 있으면 그대로 반환 / 없으면 XOR 변환하여 반환
    // SPA 방식(Header가 있음, 그대로 Header 가져와서 반환)
    // Form 방식(Header가 아닌 body의 <input name="_csrf" value="..."> 처럼 존재, value를 비교하여 반환)
    return (StringUtils.hasText(headerValue) ? this.plain : this.xor)
        .resolveCsrfTokenValue(request, csrfToken);
  }
}
