package com.sprint.mission.discodeit.entity;

import java.util.UUID;
import lombok.Getter;

@Getter
public class UserStatus extends BaseEntity {

  private final UUID userId;

  public UserStatus(UUID userId) {
    this.userId = userId;
  }
}
