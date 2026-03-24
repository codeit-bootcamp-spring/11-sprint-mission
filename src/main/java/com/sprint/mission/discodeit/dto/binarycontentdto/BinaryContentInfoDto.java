package com.sprint.mission.discodeit.dto.binarycontentdto;

import com.sprint.mission.discodeit.binary.BinaryFile;
import com.sprint.mission.discodeit.entity.BinaryContent;

import java.util.UUID;

public record BinaryContentInfoDto (

        UUID userId,
        UUID messageId,
        BinaryFile binaryFile,
        BinaryContent.Type type)
{}
