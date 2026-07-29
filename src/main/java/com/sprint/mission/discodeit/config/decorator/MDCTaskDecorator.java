package com.sprint.mission.discodeit.config.decorator;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class MDCTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable task) {
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();

    return () -> {
      try {
        if (mdcContext != null) {
          MDC.setContextMap(mdcContext);
        }
        task.run();
      } finally {
        MDC.clear();
      }
    };
  }
}
