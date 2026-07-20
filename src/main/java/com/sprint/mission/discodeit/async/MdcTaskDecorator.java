package com.sprint.mission.discodeit.async;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class MdcTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {

    // 기존 MDC, SecurityContext를 가져옴(복사)
    // MDC, SecurityContext는 각각 ThreadLocal 기반(스레드마다 따로 보관함)
    Map<String, String> contextMap = MDC.getCopyOfContextMap();
    SecurityContext securityContext = SecurityContextHolder.getContext();

    // 다른 스레드에 가져온 복사값을 붙여넣음
    return () -> {
      try {
        // MDC 복원(붙여넣기)
        if (contextMap != null) {
          MDC.setContextMap(contextMap);
        }

        // SecurityContext 복원(붙여넣기)
        SecurityContextHolder.setContext(securityContext);

        runnable.run();
      } finally {
        // Thread 재사용 방지를 위해 정리 시켜줌
        // requestId가 덮어씌워지지 않아 기존 값이 남는 등의 문제가 발생할 수 있기 때문
        MDC.clear();
        SecurityContextHolder.clearContext();
      }
    };

  }
}
