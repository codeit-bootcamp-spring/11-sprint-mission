package com.sprint.mission.discodeit.config.decorator;

import org.springframework.core.task.TaskDecorator;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityContextTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable task) {
    SecurityContext securityContext = SecurityContextHolder.getContext();

    return () -> {
      try {
        SecurityContextHolder.setContext(securityContext);
        task.run();
      } finally {
        SecurityContextHolder.clearContext();
      }
    };
  }
}
