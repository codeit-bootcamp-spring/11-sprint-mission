package com.sprint.mission.discodeit.config;

import java.util.List;
import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.CompositeTaskDecorator;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public Executor eventTaskExecutor() {
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
    executor.setCorePoolSize(5);
    executor.setMaxPoolSize(10);
    executor.setQueueCapacity(25);

    CompositeTaskDecorator decorator = new CompositeTaskDecorator(
        List.of(new MdcTaskDecorator(), new SecurityContextTaskDecorator())
    );
    executor.setTaskDecorator(decorator);

    executor.initialize();
    return executor;
  }
}
