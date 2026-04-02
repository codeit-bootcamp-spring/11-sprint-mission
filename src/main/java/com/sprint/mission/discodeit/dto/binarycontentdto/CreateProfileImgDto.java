package com.sprint.mission.discodeit.dto.binarycontentdto;


import org.springframework.web.multipart.MultipartFile;


import java.util.UUID;

public record CreateProfileImgDto(
        UUID userId,
        MultipartFile file
) {
}
