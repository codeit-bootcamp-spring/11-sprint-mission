package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {

  public static final String REQUEST_ID = "requestId";
  public static final String REQUEST_METHOD = "requestMethod";
  public static final String REQUEST_URI = "requestUri";

  public static final String REQUEST_ID_HEADER = "Discodeit-Request-ID";

  private static final String START_TIME_ATTRIBUTE = "startTime";

  @Override
  public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
      Object handler) {
    String requestId = UUID.randomUUID().toString().replaceAll("-", "");

    // MDC에 로그백 패턴 매핑용 데이터 주입
    MDC.put(REQUEST_ID, requestId);
    MDC.put(REQUEST_METHOD, request.getMethod());
    MDC.put(REQUEST_URI, request.getRequestURI());

    // 응답 헤더에 요청 ID 추가
    response.setHeader(REQUEST_ID_HEADER, requestId);

    // 요청 시작 시간을 request 객체에 임시 저장
    request.setAttribute(START_TIME_ATTRIBUTE, System.currentTimeMillis());

    log.debug("Request started");
    return true;
  }

  @Override
  public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
      Object handler, Exception ex) {

    // 요청 처리 시 계산
    long startTime = (Long) request.getAttribute(START_TIME_ATTRIBUTE);
    long elapsedTime = System.currentTimeMillis() - startTime;

    int statusCode = response.getStatus();

    log.info("Request completed: status={}, elapsedTime={}ms", statusCode, elapsedTime);
    MDC.clear();
  }
}