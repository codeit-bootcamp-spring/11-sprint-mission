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
public class ReadStatusCreateRequest {

  @NotNull(message = "userId는 필수입니다.")
  private UUID userId;

  @NotNull(message = "channelId는 필수입니다.")
  private UUID channelId;

  private Instant lastReadAt;
}
