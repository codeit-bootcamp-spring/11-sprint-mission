package com.sprint.mission.discodeit.dto.binarycontentdto;

import com.sprint.mission.discodeit.binary.BinaryFile;


import java.util.UUID;

public record CreateProfileImgDto(
        UUID userId,
        BinaryFile binaryFile
) {
}
