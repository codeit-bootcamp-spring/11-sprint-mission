package com.sprint.mission.discodeit.config;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * 비동기 스레드에서도 요청을 보낸 스레드의 MDC(Request ID 등)와 SecurityContext(인증 정보)가
 * 유지되도록 전파하는 TaskDecorator입니다.
 * <p>
 * 작업을 제출한 스레드에서 현재 컨텍스트를 캡처해두었다가, 실제로 작업이 실행되는
 * 비동기 스레드에 그 컨텍스트를 복원하고, 작업이 끝나면 정리합니다.
 */
public class ContextPropagatingTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    SecurityContext securityContext = SecurityContextHolder.getContext();

    return () -> {
      try {
        if (contextMap != null) {
          MDC.setContextMap(contextMap);
        }
        SecurityContextHolder.setContext(securityContext);
        runnable.run();
      } finally {
        MDC.clear();
        SecurityContextHolder.clearContext();
      }
    };
  }
}
