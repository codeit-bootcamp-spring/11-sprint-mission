package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "binary_contents")
public class BinaryContent extends BaseEntity {

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private Long fileSize;

  @Column(nullable = false)
  private String contentType;

  public BinaryContent(String fileName, Long fileSize, String contentType) {
    this.fileName = fileName;
    this.fileSize = fileSize;
    this.contentType = contentType;
  }
}