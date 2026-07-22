package com.sprint.mission.discodeit.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.SyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

@TestConfiguration
public class TestAsyncConfig {

  @Bean(name = "eventTaskExecutor")
  @Primary
  public TaskExecutor eventTaskExecutor() {
    return new SyncTaskExecutor();
  }
}
