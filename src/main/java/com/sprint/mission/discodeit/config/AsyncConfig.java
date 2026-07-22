package com.sprint.mission.discodeit.config;


import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@EnableAsync
@Configuration
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public TaskExecutor taskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("event-");
    executor.setTaskDecorator(task -> {

      //mdc랑 로그인 정보 저장
      Map<String, String> mdcContext = MDC.getCopyOfContextMap();
      SecurityContext securityContext = SecurityContextHolder.getContext();

      return () -> {
        try {
          if (mdcContext != null) {
            MDC.setContextMap(mdcContext);
          }
          SecurityContextHolder.setContext(securityContext);
          task.run(); // 원래 하려던 작업(예: on(MessageCreatedEvent))
        } finally {
          //스레드풀 비워놓기
          MDC.clear();
          SecurityContextHolder.clearContext();
        }


      };

    });
    executor.initialize();
    return executor;
  }

}
