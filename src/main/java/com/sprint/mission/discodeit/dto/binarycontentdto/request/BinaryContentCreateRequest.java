package com.sprint.mission.discodeit.dto.binarycontentdto.request;


import jakarta.validation.constraints.NotBlank;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

public record BinaryContentCreateRequest(

    @NotBlank(message = "유저 아이디는 필수 입력값입니다.")
    UUID userId,
    @NotBlank(message = "메시지 아이디는 필수 입력값입니다.")
    UUID messageId,
    MultipartFile binaryFile

) {

}
