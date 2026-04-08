package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.exception.DiscodeitInvalidInputException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseEntity {

  @Column(name = "file_name", nullable = false, length = 255)
  private String fileName;

  @Column(name = "size", nullable = false)
  private Long size;

  @Column(name = "content_type", nullable = false, length = 100)
  private String contentType;

  @Column(name = "bytes", nullable = false)
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