package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {

  // 컨트롤러 도달 전에 꼬리표를 붙여서 보냄
  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    String requestId = UUID.randomUUID().toString().substring(0, 8); // 8자리 랜덤 UUID 생성

    // MDC(스레드 전용 개인 사물함)에 데이터 저장
    MDC.put("requestId", requestId);
    MDC.put("method", request.getMethod());
    MDC.put("url", request.getRequestURI());

    response.setHeader("Discodeit-Request-ID",
        requestId); // 응답 헤더에 요청 ID를 세팅하므로 누가 에러가 났는지 바로 찾을 수 있음
    return true; // 컨트롤러로 보내던 요청을 계속 진행시킴 (false 반환 시 이후 처리 중단)
  }

  // 모든 응답이 끝난 후
  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {
    MDC.clear(); // 스레드가 스레드 풀로 인해 재사용되는 경우에, 이전 요청 Id가 남아있지 않도록 비움
  }
}
