package com.sprint.mission.discodeit.dto.message;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageCreateRequest {
    @NotBlank
    private String content;
    private UUID senderId;
    private UUID channelId;
    private List<UUID> attachmentIds;
}