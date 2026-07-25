package com.sprint.mission.discodeit.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class MdcTaskDecoratorTest {

    private final MdcTaskDecorator taskDecorator = new MdcTaskDecorator();

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    @DisplayName("MDC를 전파하고 작업 실행 후 정리한다")
    void decorate_propagatesAndClearsMdc() {
        MDC.put("requestId", "request-id");
        AtomicReference<String> propagatedRequestId = new AtomicReference<>();

        Runnable decoratedTask = taskDecorator.decorate(
                () -> propagatedRequestId.set(MDC.get("requestId"))
        );
        MDC.clear();

        decoratedTask.run();

        assertThat(propagatedRequestId.get()).isEqualTo("request-id");
        assertThat(MDC.getCopyOfContextMap()).isNull();
    }
}
