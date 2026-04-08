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
public class MessageUpdateRequest {

  @NotNull(message = "messageId는 필수입니다.")
  private UUID messageId;

  @NotBlank(message = "newContent는 필수입니다.")
  private String newContent;
}
