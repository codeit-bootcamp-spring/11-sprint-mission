package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.UserStatus;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class UserStatusResponseDto {

  private UUID id;
  private UUID userId;
  private Instant lastAccessAt;
  private Instant newLastActiveAt;  // 프론트가 보내는 필드명 추가
  private boolean online;

  public UserStatusResponseDto(UserStatus status) {
    this.id = status.getId();
    this.userId = status.getUserId();
    this.lastAccessAt = status.getLastAccessAt();
    this.online = status.isOnline();
  }

  // 둘 중 하나라도 있으면 반환
  public Instant getLastAccessAt() {
    return newLastActiveAt != null ? newLastActiveAt : lastAccessAt;
  }
}
