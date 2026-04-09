package com.sprint.mission.discodeit.dto.request.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class CreateMessageRequest {
    private String content;
    private UUID channelId;
    private UUID userId;
}
