package com.sprint.mission.discodeit.dto.binarycontentdto;


import com.sprint.mission.discodeit.entity.BinaryContent;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record CreateBinaryContentDto(

        UUID userId,
        UUID messageId,
        MultipartFile binaryFile

) {
}
