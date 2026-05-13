package com.sprint.mission.discodeit.dto.request;

import com.sprint.mission.discodeit.entity.BinaryContent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record BinaryContentCreateRequest(
    @NotBlank(message = "파일 이름은 필수입니다.")
    String fileName,

    @NotNull(message = "파일 크기는 필수입니다.")
    @Positive(message = "파일 크기는 0보다 커야 합니다.")
    Long size, // 1 이상

    @NotBlank(message = "파일 형식은 필수입니다.")
    String contentType,

    @NotNull(message = "파일 데이터는 필수입니다.")
    @Size(min = 1, message = "파일 데이터가 비어있습니다.")
    byte[] bytes // 1 이상
) {

  // userId가 없을 경우 첨부파일을, 있을 경우 프로필 이미지를 binaryContent로 설정
  public BinaryContent toBinaryContent() {
    return BinaryContent.of(fileName, size, contentType);
  }
}
// 원래라면 @JsonProperty를 매개변수 앞에 붙이고, @JsonCreator을 사용하여 json에서 자바객체로 역직렬화 시 생성자가 있어야 했으나
// JDK 14 이후 Jackson이 record를 인식하고 자동으로 처리를 해주기 때문에 @JsonCreator, @JsonProperty 생략이 가능해졌다.\

// NotBlank : String
// NotNull : Long, byte 등..
// Size : byte
// Positive : Long, int 등..