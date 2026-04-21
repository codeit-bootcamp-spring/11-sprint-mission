package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseEntity {

  @Column(nullable = false, updatable = false)
  private String fileName;

  @Column(nullable = false, updatable = false)
  private Long size;

  @Column(length = 100, nullable = false, updatable = false)
  private String contentType;

  public BinaryContent(String fileName, Long size, String contentType) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
  }
}
