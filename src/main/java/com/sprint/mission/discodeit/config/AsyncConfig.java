package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.config.decorator.MdcTaskDecorator;
import com.sprint.mission.discodeit.config.decorator.SecurityContextTaskDecorator;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableAsync
@Configuration
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public TaskExecutor eventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(10);
    executor.setMaxPoolSize(30);
    executor.setQueueCapacity(50);
    executor.setThreadNamePrefix("event-");

    executor.setTaskDecorator(new CompositeTaskDecorator(
        List.of(new MdcTaskDecorator(), new SecurityContextTaskDecorator())
    ));
    executor.initialize();
    return executor;
  }
}
