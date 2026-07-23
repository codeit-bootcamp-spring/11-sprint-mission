package com.sprint.mission.discodeit.event;

import java.time.Instant;

public abstract class DeletedEvent<T> {

  private final T data;
  private final Instant deletedAt;

  protected DeletedEvent(T data, Instant deletedAt) {
    this.data = data;
    this.deletedAt = deletedAt;
  }

  public T getData() {
    return data;
  }

  public Instant getDeletedAt() {
    return deletedAt;
  }
}