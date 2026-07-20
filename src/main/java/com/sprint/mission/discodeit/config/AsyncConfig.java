package com.sprint.mission.discodeit.config;

import com.sprint.mission.discodeit.async.MdcTaskDecorator;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
@RequiredArgsConstructor
public class AsyncConfig {

  @Bean(name = "eventTaskExecutor")
  public TaskExecutor eventExecutor() {

    // 비동기 작업을 처리할 스레드 풀 구현체
    ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

    // 기본 유지 스레드 개수 설정(작업이 없어도 항상 유지되는 스레드 개수 설정)
    executor.setCorePoolSize(5);

    // 최대 스레드 개수 설정(작업이 많아질 경우 최대 스레드 개수 설정)
    executor.setMaxPoolSize(10);

    // 대기 큐 크기 설정(스레드가 모두 할당되어 작업 중일 경우 작업을 저장하는 공간 크기 설정)
    executor.setQueueCapacity(100);

    // 스레드 이름 지정
    executor.setThreadNamePrefix("event-");

    // 비동기 스레드로 작업이 넘어 갈 경우 TaskDecorator를 사용하여 커스텀 Context들을 복사/붙여넣기
    executor.setTaskDecorator(new MdcTaskDecorator());

    // set.. 메서드로 설정한 값들을 통해 Executor을 초기화
    executor.initialize();

    // AsyncTaskExecutor(extends TaskExecutor)
    return executor;
  }

}
