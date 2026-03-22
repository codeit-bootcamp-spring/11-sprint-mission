package com.sprint.mission.discodeit.dto.message;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class MessageCreateRequest {
    private String content;
    private UUID senderId;
    private UUID channelId;
    private List<UUID> attachmentIds;
}