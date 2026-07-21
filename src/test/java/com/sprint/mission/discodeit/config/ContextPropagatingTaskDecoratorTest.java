package com.sprint.mission.discodeit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

class ContextPropagatingTaskDecoratorTest {

  private final ContextPropagatingTaskDecorator decorator = new ContextPropagatingTaskDecorator();

  @AfterEach
  void tearDown() {
    MDC.clear();
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("작업을 제출한 스레드의 MDC와 SecurityContext가 비동기 스레드에 전파된다")
  void decorate_PropagatesMdcAndSecurityContext() throws InterruptedException {
    // given
    MDC.put("requestId", "test-request-id");

    Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", null,
        List.of());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    AtomicReference<String> capturedRequestId = new AtomicReference<>();
    AtomicReference<String> capturedUsername = new AtomicReference<>();

    Runnable task = () -> {
      capturedRequestId.set(MDC.get("requestId"));
      Authentication current = SecurityContextHolder.getContext().getAuthentication();
      capturedUsername.set(current != null ? current.getName() : null);
    };

    // when
    Runnable decorated = decorator.decorate(task);
    Thread thread = new Thread(decorated);
    thread.start();
    thread.join();

    // then
    assertThat(capturedRequestId.get()).isEqualTo("test-request-id");
    assertThat(capturedUsername.get()).isEqualTo("testuser");
  }

  @Test
  @DisplayName("작업이 끝나면 실행 스레드의 MDC와 SecurityContext가 정리된다")
  void decorate_ClearsContextAfterExecution() throws InterruptedException {
    // given
    MDC.put("requestId", "test-request-id");
    Authentication authentication = new UsernamePasswordAuthenticationToken("testuser", null,
        List.of());
    SecurityContext context = SecurityContextHolder.createEmptyContext();
    context.setAuthentication(authentication);
    SecurityContextHolder.setContext(context);

    AtomicReference<Object> afterMdc = new AtomicReference<>("not-run");
    AtomicReference<Authentication> afterAuth = new AtomicReference<>();

    Runnable decorated = decorator.decorate(() -> {
      // no-op: 작업 본체
    });

    // when
    Thread thread = new Thread(() -> {
      decorated.run();
      afterMdc.set(MDC.get("requestId"));
      afterAuth.set(SecurityContextHolder.getContext().getAuthentication());
    });
    thread.start();
    thread.join();

    // then
    assertThat(afterMdc.get()).isNull();
    assertThat(afterAuth.get()).isNull();
  }
}
