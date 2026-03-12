package com.sprint.mission.discodeit.dto.request.message;

import com.sprint.mission.discodeit.dto.request.binaryContent.CreateBinaryContentRequest;
import lombok.Getter;

import java.util.List;
import java.util.UUID;

@Getter
public class CreateMessageRequest {
    private String content;
    private UUID channelId;
    private UUID userId;
    List<CreateBinaryContentRequest> attachments;
}
