package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseUpdatableEntity {

  @Column(nullable = false, updatable = false)
  private String fileName;

  @Column(nullable = false, updatable = false)
  private Long size;

  @Column(length = 100, nullable = false, updatable = false)
  private String contentType;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BinaryContentStatus status = BinaryContentStatus.PROCESSING;

  public BinaryContent(String fileName, Long size, String contentType) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
  }

  public void updateStatus(BinaryContentStatus status) {
    this.status = status;
  }
}
