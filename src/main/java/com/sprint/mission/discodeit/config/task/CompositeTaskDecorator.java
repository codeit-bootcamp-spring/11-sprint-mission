package com.sprint.mission.discodeit.config.task;

import java.util.List;
import org.springframework.core.task.TaskDecorator;

public class CompositeTaskDecorator implements TaskDecorator {

  private final List<TaskDecorator> delegates;

  public CompositeTaskDecorator(List<TaskDecorator> delegates) {
    this.delegates = List.copyOf(delegates);
  }

  @Override
  public Runnable decorate(Runnable runnable) {
    Runnable decorated = runnable;

    for (int i = delegates.size() - 1; i >= 0; i--) {
      decorated = delegates.get(i).decorate(decorated);
    }
    return decorated;
  }
}
