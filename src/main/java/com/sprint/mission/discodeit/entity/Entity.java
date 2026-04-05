package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
public abstract class Entity implements Serializable {

  @Serial
  private static final long serialVersionUID = 1L;
  private final UUID id; // 인스턴스 아이디
  private final Instant createdAt; //생성 시간
  private Instant updatedAt; //변경 시간


  public Entity() {

    id = UUID.randomUUID();
    createdAt = Instant.now();
    updatedAt = Instant.now();
  }


  public void updateUpdatedAt() {

    this.updatedAt = Instant.now();
  }


  public int compareTo(Entity entity) {
    return this.createdAt.compareTo(entity.getCreatedAt());
  }
}
