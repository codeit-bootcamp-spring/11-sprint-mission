package com.sprint.mission.discodeit.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

class ContextPropagatingTaskDecoratorTest {

  private final ContextPropagatingTaskDecorator taskDecorator =
      new ContextPropagatingTaskDecorator();

  @AfterEach
  void tearDown() {
    MDC.clear();
    SecurityContextHolder.clearContext();
  }

  @Test
  @DisplayName("MDC의 Request ID와 인증 정보가 다른 스레드로 전파된다")
  void decorate_PropagatesMdcAndSecurityContext() throws Exception {
    
    MDC.put(MDCLoggingInterceptor.REQUEST_ID, "test-request-id");
    Authentication authentication =
        new UsernamePasswordAuthenticationToken("tester", null, List.of());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    AtomicReference<String> requestId = new AtomicReference<>();
    AtomicReference<Authentication> propagatedAuthentication = new AtomicReference<>();
    Runnable task = taskDecorator.decorate(() -> {
      requestId.set(MDC.get(MDCLoggingInterceptor.REQUEST_ID));
      propagatedAuthentication.set(SecurityContextHolder.getContext().getAuthentication());
    });

   
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      CompletableFuture.runAsync(task, executor).get();
    } finally {
      executor.shutdown();
    }

  
    assertThat(requestId.get()).isEqualTo("test-request-id");
    assertThat(propagatedAuthentication.get()).isEqualTo(authentication);
  }

  @Test
  @DisplayName("작업이 끝나면 실행 스레드의 컨텍스트를 정리한다")
  void decorate_ClearsContextAfterRun() throws Exception {

    MDC.put(MDCLoggingInterceptor.REQUEST_ID, "test-request-id");
    SecurityContextHolder.getContext().setAuthentication(
        new UsernamePasswordAuthenticationToken("tester", null, List.of()));

    Runnable task = taskDecorator.decorate(() -> {
    });

    AtomicReference<String> leakedRequestId = new AtomicReference<>();
    AtomicReference<Authentication> leakedAuthentication = new AtomicReference<>();

  
    ExecutorService executor = Executors.newSingleThreadExecutor();
    try {
      CompletableFuture.runAsync(task, executor).get();
      CompletableFuture.runAsync(() -> {
        leakedRequestId.set(MDC.get(MDCLoggingInterceptor.REQUEST_ID));
        leakedAuthentication.set(SecurityContextHolder.getContext().getAuthentication());
      }, executor).get();
    } finally {
      executor.shutdown();
    }

   
    assertThat(leakedRequestId.get()).isNull();
    assertThat(leakedAuthentication.get()).isNull();
  }
}