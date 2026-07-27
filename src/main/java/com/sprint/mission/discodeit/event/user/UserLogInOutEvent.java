package com.sprint.mission.discodeit.event.user;

import java.util.UUID;
import lombok.Getter;

@Getter
public class UserLogInOutEvent {

  private final UUID userId;
  private final boolean login;

  public UserLogInOutEvent(UUID userId, boolean login) {
    this.userId = userId;
    this.login = login;
  }

}
