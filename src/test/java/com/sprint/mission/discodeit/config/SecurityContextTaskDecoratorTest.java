package com.sprint.mission.discodeit.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityContextTaskDecoratorTest {

    private final SecurityContextTaskDecorator taskDecorator =
            new SecurityContextTaskDecorator();

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("SecurityContext를 전파하고 작업 실행 후 정리한다")
    void decorate_propagatesAndClearsSecurityContext() {
        Authentication authentication =
                new TestingAuthenticationToken("user", null, "ROLE_USER");
        SecurityContextHolder.getContext().setAuthentication(authentication);
        AtomicReference<Authentication> propagatedAuthentication =
                new AtomicReference<>();

        Runnable decoratedTask = taskDecorator.decorate(
                () -> propagatedAuthentication.set(
                        SecurityContextHolder.getContext().getAuthentication()
                )
        );
        SecurityContextHolder.clearContext();

        decoratedTask.run();

        assertThat(propagatedAuthentication.get()).isSameAs(authentication);
        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
    }
}
