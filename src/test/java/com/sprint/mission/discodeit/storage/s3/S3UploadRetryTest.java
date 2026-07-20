package com.sprint.mission.discodeit.storage.s3;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.sprint.mission.discodeit.config.MDCLoggingInterceptor;
import com.sprint.mission.discodeit.event.S3UploadFailedEvent;
import com.sprint.mission.discodeit.storage.BinaryContentStorage;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

@SpringJUnitConfig(S3UploadRetryTest.TestConfig.class)
class S3UploadRetryTest {

  @Autowired
  private BinaryContentStorage storage;

  @Autowired
  private FailureEventRecorder recorder;

  @AfterEach
  void tearDown() {
    MDC.clear();
    recorder.events.clear();
    clearInvocations(TestConfig.spyStorage);
  }

  @Test
  @DisplayName("업로드 실패 시 최대 3회까지 재시도한다")
  void put_RetriesUpToThreeTimes() {
    // given
    UUID binaryContentId = UUID.randomUUID();

    // when
    assertThatThrownBy(() -> storage.put(binaryContentId, new byte[0]))
        .isInstanceOf(RuntimeException.class);

    // then
    verify(TestConfig.spyStorage, times(3)).put(eq(binaryContentId), any(byte[].class));
  }

  @Test
  @DisplayName("재시도가 모두 실패하면 작업 이름, Request ID, 실패 이유를 담은 이벤트를 발행한다")
  void put_PublishesFailureEventAfterRetriesExhausted() {
    // given
    MDC.put(MDCLoggingInterceptor.REQUEST_ID, "test-request-id");
    UUID binaryContentId = UUID.randomUUID();

    // when
    assertThatThrownBy(() -> storage.put(binaryContentId, new byte[0]))
        .isInstanceOf(RuntimeException.class);

    // then
    assertThat(recorder.events).hasSize(1);
    S3UploadFailedEvent event = recorder.events.get(0);
    assertThat(event.taskName()).isEqualTo("S3 파일 업로드");
    assertThat(event.requestId()).isEqualTo("test-request-id");
    assertThat(event.binaryContentId()).isEqualTo(binaryContentId);
    assertThat(event.errorMessage()).isNotBlank();
  }

  @EnableRetry
  @Configuration
  static class TestConfig {

    // 재시도 프록시가 감싸기 전의 스파이를 재시도 횟수 검증에 사용한다
    static S3BinaryContentStorage spyStorage;

    @Bean
    BinaryContentStorage s3BinaryContentStorage(ApplicationEventPublisher eventPublisher) {
      spyStorage = spy(new S3BinaryContentStorage("", "", "ap-northeast-2", "test-bucket",
          eventPublisher));
      return spyStorage;
    }

    @Bean
    FailureEventRecorder failureEventRecorder() {
      return new FailureEventRecorder();
    }
  }

  static class FailureEventRecorder {

    final List<S3UploadFailedEvent> events = new CopyOnWriteArrayList<>();

    @EventListener
    void on(S3UploadFailedEvent event) {
      events.add(event);
    }
  }
}