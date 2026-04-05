package com.sprint.mission.discodeit.dto.messagedto;


import java.util.UUID;

public record CreateMessageDto(

    String content,
    UUID channelId,
    UUID authorId
    
) {

}
