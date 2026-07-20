package com.sprint.mission.discodeit.config;

import static org.assertj.core.api.Assertions.assertThat;

import io.micrometer.core.aop.TimedAspect;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AsyncConfigTest {

  @Autowired
  private ThreadPoolTaskExecutor eventTaskExecutor;

  @Autowired(required = false)
  private TimedAspect timedAspect;

  @Test
  @DisplayName("이벤트 처리용 Executor에 컨텍스트 전파 데코레이터가 설정된다")
  void eventTaskExecutor_HasContextPropagatingTaskDecorator() {
    assertThat(eventTaskExecutor.getThreadNamePrefix()).isEqualTo("event-");
    assertThat(eventTaskExecutor)
        .extracting("taskDecorator")
        .isInstanceOf(ContextPropagatingTaskDecorator.class);
  }

  @Test
  @DisplayName("@Timed 측정을 위한 TimedAspect가 등록된다")
  void timedAspect_IsRegistered() {
    assertThat(timedAspect).isNotNull();
  }
}
