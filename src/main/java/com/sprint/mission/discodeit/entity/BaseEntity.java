package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BaseEntity implements Serializable, Identifiable {

  private static final long serialVersionUID = 1L;
  private final UUID id;
  private final Instant createdAt;
  private Instant updatedAt;

  public BaseEntity() {
    this.id = UUID.randomUUID();
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public void setUpdatedAt() {
    this.updatedAt = Instant.now();
  }
}
