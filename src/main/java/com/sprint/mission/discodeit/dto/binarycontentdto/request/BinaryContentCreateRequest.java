package com.sprint.mission.discodeit.dto.binarycontentdto.request;


import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record BinaryContentCreateRequest(

    UUID userId,
    UUID messageId,
    MultipartFile binaryFile

) {

}
