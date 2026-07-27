package com.sprint.mission.discodeit.config.task;

import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    SecurityContext securityContext = SecurityContextHolder.getContext();

    return () -> {
      SecurityContext previous = SecurityContextHolder.getContext();
      try {
        SecurityContextHolder.setContext(securityContext);
        runnable.run();
      } finally {
        SecurityContextHolder.setContext(previous);
      }
    };
  }
}
