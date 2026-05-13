package com.sprint.mission.discodeit.service.dto.message;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

public record CreateMessageRequest(
        @NotNull UUID authorId,
        @NotNull UUID channelId,
        @NotBlank String content,
        List<MessageAttachmentRequest> attachments
) {
}
