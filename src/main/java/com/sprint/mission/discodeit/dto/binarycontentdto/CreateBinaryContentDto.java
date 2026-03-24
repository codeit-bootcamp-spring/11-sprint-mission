package com.sprint.mission.discodeit.dto.binarycontentdto;

import com.sprint.mission.discodeit.binary.BinaryFile;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public record CreateBinaryContentDto(

        UUID userId,
        UUID messageId,
        BinaryContent.Type type,
        BinaryFile binaryFile

) {
}
