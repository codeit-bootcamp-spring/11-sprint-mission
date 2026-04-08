package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MessageCreateRequest {

  @NotBlank(message = "content는 필수입니다.")
  private String content;

  @NotNull(message = "channelId는 필수입니다.")
  private UUID channelId;

  @NotNull(message = "authorId는 필수입니다.")
  private UUID authorId;
}
