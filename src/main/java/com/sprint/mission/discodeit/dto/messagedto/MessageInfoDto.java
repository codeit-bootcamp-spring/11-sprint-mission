package com.sprint.mission.discodeit.dto.messagedto;


import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record MessageInfoDto(

    UUID id,
    Instant createdAt,
    Instant updatedAt,
    String content,
    UUID channelId,
    UUID authorId,
    List<UUID> attachmentIds

) {

}
