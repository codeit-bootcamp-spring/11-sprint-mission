package com.sprint.mission.discodeit.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class MDCLoggingInterceptor implements HandlerInterceptor {

    private final String REQUEST_ID = "requestId";
    private final String REQUEST_METHOD = "requestMethod";
    private final String REQUEST_URL = "requestUrl";

    private static final String HEADER_REQUEST_ID = "Discodeit-Request-ID";

    // 요청 처리 전
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 1. 요청 ID 생성
        String requestId = UUID.randomUUID().toString();

        // 2. MDC에 정보 저장
        MDC.put(REQUEST_ID, requestId);
        MDC.put(REQUEST_METHOD, request.getMethod());
        MDC.put(REQUEST_URL, request.getRequestURI());

        // 3. 응답 헤더에 Request ID 포함
        response.setHeader(HEADER_REQUEST_ID, requestId);

        return true; // true를 반환해야 다음 단계(컨트롤러)로 넘어감
    }

    // 요청 처리 후
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 톰캣의 쓰레드 풀 재사용으로 인한 정보 꼬임 및 메모리 누수를 막기 위해서 반드시 비워줘야함
        MDC.clear();
    }

    // 전체 동작 흐름
    // - HTTP 요청 도착: 클라이언트 -> 서버로 요청
    // - Interceptor: DispatcherServlet이 컨트롤러를 호출하기 전, 등록된 인터셉터의 preHandle을 실행
    // - 컨텍스트 설정: 쓰레드 전용 저장소(MDC)에 requestId 등을 저장
    // - 비즈니스 로직 수행: 로그가 남을 때마다 MDC의 정보가 자동으로 로그 메세지에 포함됨
    // - 응답 생성: 처리가 끝나면 응답 헤더에 requestId를 실어서 보냄
    // - 자원 정리: afterCompletion이 실행되면서 MDC 정보를 삭제
    //      - 삭제 이유: 쓰레드 풀을 사용하므로 같은 쓰레드 재사용 시 값이 남아있는 경우가 발생할 수 있음
}
