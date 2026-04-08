package com.sprint.mission.discodeit.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class UserStatus extends BaseEntity {

  private final UUID userId;
  private Instant lastActiveAt;

  public UserStatus(UUID userId) {
    super();
    this.userId = userId;
    this.lastActiveAt = Instant.now();
  }


  // 온라인 여부 확인 메서드
  public boolean isOnline() {
    // 접속 시간 5분 이내: 온라인
    return lastActiveAt.isAfter(Instant.now().minusSeconds(300));
  }

  public void updateActiveTime(Instant lastActiveAt) {
    if (lastActiveAt != null && !lastActiveAt.equals(this.lastActiveAt)) {
      this.lastActiveAt = lastActiveAt;
      super.timeUpdate();
    }
  }
}