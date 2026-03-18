package com.sprint.mission.discodeit.dto.message;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class MessageCreateRequest {
    private String content;
    private UUID authorId;
    private UUID channelId;
    private UUID receiverId;
    private String fileName;
    private byte[] fileContent;
    private String contentType;
}
