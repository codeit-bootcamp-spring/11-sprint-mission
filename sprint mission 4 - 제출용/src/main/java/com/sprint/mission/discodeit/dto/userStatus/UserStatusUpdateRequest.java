package com.sprint.mission.discodeit.dto.userStatus;

import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserStatusUpdateRequest {

  @NotNull(message = "userId는 필수입니다.")
  private UUID userId;

  @NotNull(message = "newLastActiveAt은 필수입니다.")
  private Instant newLastActiveAt;
}
