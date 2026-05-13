package com.sprint.mission.discodeit.dto;

import com.sprint.mission.discodeit.entity.BinaryContent;
import com.sprint.mission.discodeit.exception.binarycontent.FileOperationException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import lombok.Builder;
import org.springframework.web.multipart.MultipartFile;

public class BinaryContentDto {

  @Builder
  public record CreateRequest(
      @NotBlank(message = "파일명은 필수 항목입니다.")
      @Size(max = 255, message = "파일명은 255자를 초과할 수 없습니다.")
      String fileName,

      @NotBlank(message = "콘텐츠 타입은 필수 항목입니다.")
      @Size(max = 100, message = "콘텐츠 타입은 100자를 초과할 수 없습니다.")
      String contentType,

      Long size,

      @NotEmpty(message = "파일 데이터가 비어있습니다.")
      byte[] bytes
  ) {

    // 단건 MultipartFile -> DTO
    public static CreateRequest of(MultipartFile file) {
      // 파일이 없거나 비어있는 경우 null 반환
      if (file == null || file.isEmpty()) {
        return null;
      }

      try {
        return CreateRequest.builder()
            .fileName(file.getOriginalFilename())
            .contentType(file.getContentType())
            .size(file.getSize())
            .bytes(file.getBytes())
            .build();
      } catch (IOException e) {
        throw FileOperationException.readFailed();
      }
    }

    // 다건 MultipartFile -> DTO
    public static List<CreateRequest> ofList(List<MultipartFile> files) {
      return Optional.ofNullable(files)
          .orElse(Collections.emptyList())
          .stream()
          .map(CreateRequest::of)
          .filter(Objects::nonNull)
          .toList();
    }

    // DTO -> Entity
    public BinaryContent toEntity() {
      return BinaryContent.builder()
          .fileName(this.fileName)
          .contentType(this.contentType)
          .size(this.size)
          .build();
    }
  }

  @Builder
  public record Response(
      UUID id,
      String fileName,
      Long size,
      String contentType,
      byte[] bytes
  ) {

  }
}