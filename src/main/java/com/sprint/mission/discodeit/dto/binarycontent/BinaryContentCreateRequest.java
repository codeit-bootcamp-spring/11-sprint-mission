package com.sprint.mission.discodeit.dto.binarycontent;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

public record BinaryContentCreateRequest(
    @NotBlank(message = "파일 이름은 필수입니다.")
    String fileName,

    @NotBlank(message = "컨텐츠 타입은 필수입니다.")
    String contentType,

    @NotEmpty(message = "파일 내용이 비어있을 수 없습니다.")
    byte[] bytes
) {

}
