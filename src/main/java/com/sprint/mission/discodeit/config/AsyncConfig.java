package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.config.decorator.MDCTaskDecorator;
import com.sprint.mission.discodeit.config.decorator.SecurityContextTaskDecorator;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public TaskExecutor eventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(50);
    executor.setQueueCapacity(100);
    executor.setThreadNamePrefix("event-");

    CompositeTaskDecorator compositeTaskDecorator = new CompositeTaskDecorator(
        List.of(
            new MDCTaskDecorator(),
            new SecurityContextTaskDecorator()
        )
    );

    executor.setTaskDecorator(compositeTaskDecorator);
    executor.initialize();

    return executor;
  }
}
