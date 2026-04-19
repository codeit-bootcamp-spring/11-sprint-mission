package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import java.util.UUID;

public record MessageCreateRequest(
    @NotBlank String content,
    UUID channelId,
    UUID authorId
) {

}