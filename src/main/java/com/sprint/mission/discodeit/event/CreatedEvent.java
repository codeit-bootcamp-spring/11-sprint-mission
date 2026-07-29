package com.sprint.mission.discodeit.event;

import java.time.Instant;

public abstract class CreatedEvent<T> {

  private final T data;
  private final Instant createdAt;

  protected CreatedEvent(T data, Instant createdAt) {
    this.data = data;
    this.createdAt = createdAt;
  }

  public T getData() {
    return data;
  }

  public Instant getCreatedAt() {
    return createdAt;
  }
}