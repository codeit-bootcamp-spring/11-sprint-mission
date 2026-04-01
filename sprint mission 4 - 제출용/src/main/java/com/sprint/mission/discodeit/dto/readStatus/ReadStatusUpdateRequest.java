package com.sprint.mission.discodeit.dto.readStatus;

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
public class ReadStatusUpdateRequest {

  @NotNull(message = "readStatusId는 필수입니다.")
  private UUID readStatusId;

  @NotNull(message = "newLastReadAt은 필수입니다.")
  private Instant newLastReadAt;
}
