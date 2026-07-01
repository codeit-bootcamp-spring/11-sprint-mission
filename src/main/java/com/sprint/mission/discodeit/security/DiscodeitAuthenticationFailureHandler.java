package com.sprint.mission.discodeit.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;

@Component
public class DiscodeitAuthenticationFailureHandler implements AuthenticationFailureHandler {

  @Override
  public void onAuthenticationFailure(
      HttpServletRequest request,
      HttpServletResponse response,
      AuthenticationException exception
  ) throws IOException, ServletException {
    // 로그인 실패 시 인증 실패 상태 코드 반환함
    // 상세 에러 응답 형식은 추후 GlobalExceptionHandler 스타일에 맞춰 정리 가능함
    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid username or password");
  }
}