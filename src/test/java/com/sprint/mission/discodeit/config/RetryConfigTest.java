package com.sprint.mission.discodeit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;

/**
 * RetryConfig(@EnableRetry)가 실제로 @Retryable / @Recover 애노테이션을 프록시로 감싸
 * 동작시키는지 확인하는 테스트입니다. 실제 AWS/S3 호출 없이, 스프링 컨텍스트만 부팅해
 * 재시도 인프라 자체를 검증합니다(S3BinaryContentStorage와 동일한 패턴의 최소 예시).
 */
class RetryConfigTest {

  private AnnotationConfigApplicationContext context;

  @AfterEach
  void tearDown() {
    if (context != null) {
      context.close();
    }
  }

  @Test
  @DisplayName("일시적으로 실패하다 성공하면 재시도 끝에 정상적으로 값을 반환한다")
  void retryable_RetriesUntilSuccess() {
    // given
    context = new AnnotationConfigApplicationContext(RetryConfig.class, TestConfig.class);
    FlakyComponent flakyComponent = context.getBean(FlakyComponent.class);

    // when
    String result = flakyComponent.doWork(2); // 2번 실패 후 3번째 시도에서 성공

    // then
    assertThat(result).isEqualTo("success");
    assertThat(flakyComponent.getAttemptCount()).isEqualTo(3);
  }

  @Test
  @DisplayName("모든 재시도가 실패하면 @Recover 메소드가 대신 호출된다")
  void retryable_FallsBackToRecover_WhenAllAttemptsFail() {
    // given
    context = new AnnotationConfigApplicationContext(RetryConfig.class, TestConfig.class);
    FlakyComponent flakyComponent = context.getBean(FlakyComponent.class);

    // when
    String result = flakyComponent.doWork(Integer.MAX_VALUE); // 항상 실패

    // then
    assertThat(result).isEqualTo("recovered");
    assertThat(flakyComponent.getAttemptCount()).isEqualTo(3); // maxAttempts만큼만 시도
  }

  @Configuration
  static class TestConfig {

    @Bean
    public FlakyComponent flakyComponent() {
      return new FlakyComponent();
    }
  }

  static class FlakyComponent {

    private final AtomicInteger attemptCount = new AtomicInteger();

    @Retryable(
        retryFor = IllegalStateException.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 10, multiplier = 1)
    )
    public String doWork(int failUntilAttempt) {
      int attempt = attemptCount.incrementAndGet();
      if (attempt <= failUntilAttempt) {
        throw new IllegalStateException("일시적인 실패, attempt=" + attempt);
      }
      return "success";
    }

    @Recover
    public String recover(IllegalStateException e, int failUntilAttempt) {
      return "recovered";
    }

    public int getAttemptCount() {
      return attemptCount.get();
    }
  }
}
