package com.sprint.mission.discodeit.config.task;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class MdcTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();

    return () -> {
      Map<String, String> previous = MDC.getCopyOfContextMap();
      try {
        if (mdcContext != null) {
          MDC.setContextMap(mdcContext);
        } else {
          MDC.clear();
        }
        runnable.run();
      } finally {
        if (previous != null) {
          MDC.setContextMap(previous);
        } else {
          MDC.clear();
        }
      }
    };
  }
}
