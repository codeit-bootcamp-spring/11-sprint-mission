package com.sprint.mission.discodeit.dto.messagedto.request;


import java.util.UUID;

public record MessageCreateRequest(

    String content,
    UUID channelId,
    UUID authorId

) {

}
