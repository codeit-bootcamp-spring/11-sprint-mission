package com.sprint.mission.discodeit.dto.messagedto;


import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public record MessageInfoDto(

        UUID messageId,
        UUID senderId,
        UUID channelId,
        String content,
        List<BinaryContent> binaryContents

) {
}
