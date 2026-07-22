package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.config.task.CompositeTaskDecorator;
import com.sprint.mission.discodeit.config.task.MdcTaskDecorator;
import com.sprint.mission.discodeit.config.task.SecurityContextTaskDecorator;
import java.util.List;
import java.util.Map;
import org.slf4j.MDC;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public TaskExecutor eventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(4);
    executor.setMaxPoolSize(8);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("event-");
    executor.setTaskDecorator(new CompositeTaskDecorator(
        List.of(new MdcTaskDecorator(), new SecurityContextTaskDecorator())
    ));
    executor.initialize();
    return executor;
  }
}