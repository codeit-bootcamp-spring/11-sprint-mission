package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class BinaryContent extends BaseEntity {

  private String fileName;
  private Long size;
  private String contentType;
  private byte[] bytes;

  public BinaryContent(String fileName, Long size, String contentType, byte[] bytes) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
    this.bytes = bytes;
  }

  public void updateBinaryContent(String fileName, Long size, String contentType, byte[] bytes) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
    this.bytes = bytes;
  }

  public void validateService() {
    if (this.fileName == null || this.fileName.isBlank()) {
      throw DiscodeitInvalidInputException.blankField("fileName");
    }
    if (this.bytes == null || this.bytes.length == 0) {
      throw DiscodeitInvalidInputException.blankField("bytes");
    }
    if (this.contentType == null || this.contentType.isBlank()) {
      throw DiscodeitInvalidInputException.blankField("contentType");
    }
  }
}