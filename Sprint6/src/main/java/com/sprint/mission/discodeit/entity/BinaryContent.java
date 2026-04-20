package com.sprint.mission.discodeit.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseEntity { // 수정 불가능하므로 BaseEntity 상속 유지

  @Column(nullable = false)
  private String filename;

  @Column(nullable = false)
  private Long size;

  @Column(nullable = false)
  private String contentType;

  @Builder
  public BinaryContent(String filename, Long size, String contentType) {
    this.filename = filename;
    this.size = size;
    this.contentType = contentType;
  }
}