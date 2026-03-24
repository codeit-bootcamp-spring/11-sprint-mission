package com.sprint.mission.discodeit.dto.messagedto;

import com.sprint.mission.discodeit.binary.BinaryFile;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.List;
import java.util.UUID;

public record CreateMessageDto(

        String content,
        UUID userId,
        UUID channelId,
        List<BinaryFile> binaryFile

) {
}
