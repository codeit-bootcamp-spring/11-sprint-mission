package com.sprint.mission.discodeit.dto;

import java.time.Instant;
import java.util.UUID;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ReadStatusUpdateRequestDto {

  private UUID id;
  private Instant lastReadAt;
  private Instant newLastReadAt;  // 프론트가 보내는 필드명 추가
  private boolean isRead;

  // 둘 중 하나라도 있으면 반환
  public Instant getLastReadAt() {
    return newLastReadAt != null ? newLastReadAt : lastReadAt;
  }
}
