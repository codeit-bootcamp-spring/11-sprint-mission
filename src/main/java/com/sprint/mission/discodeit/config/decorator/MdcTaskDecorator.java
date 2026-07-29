package com.sprint.mission.discodeit.config.decorator;

import java.util.Map;
import org.slf4j.MDC;
import org.springframework.core.task.TaskDecorator;

public class MdcTaskDecorator implements TaskDecorator {

  @Override
  public Runnable decorate(Runnable runnable) {
    Map<String, String> mdcContext = MDC.getCopyOfContextMap();
    return () -> {
      try {
        if (mdcContext != null) {
          MDC.setContextMap(mdcContext);
        }
        runnable.run();
      } finally {
        MDC.clear();
      }
    };
  }

}
