package com.sprint.mission.discodeit.util;

import java.util.function.BooleanSupplier;

/**
 * @Async 리스너가 백그라운드 스레드에서 처리를 끝낼 때까지 테스트에서 폴링(polling)으로
 * 기다리기 위한 유틸리티입니다. 비동기 적용(미션 03) 이후 이벤트 리스너의 부수효과(파일 저장,
 * 알림 생성 등)는 더 이상 트랜잭션 커밋 시점에 동기적으로 끝나지 않으므로, 단순히
 * TestTransaction.end() 이후 바로 검증하면 타이밍에 따라 실패(flaky)할 수 있습니다.
 */
public final class AsyncTestUtils {

  private static final long DEFAULT_POLL_INTERVAL_MILLIS = 100;

  private AsyncTestUtils() {
  }

  public static void awaitUntil(BooleanSupplier condition, long timeoutMillis) {
    long deadline = System.currentTimeMillis() + timeoutMillis;
    while (System.currentTimeMillis() < deadline) {
      if (condition.getAsBoolean()) {
        return;
      }
      try {
        Thread.sleep(DEFAULT_POLL_INTERVAL_MILLIS);
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        throw new RuntimeException("대기 중 인터럽트가 발생했습니다.", e);
      }
    }
    throw new AssertionError("조건이 " + timeoutMillis + "ms 안에 충족되지 않았습니다.");
  }
}
