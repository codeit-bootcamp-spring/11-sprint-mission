package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "binary_contents")
@NoArgsConstructor
public class BinaryContent extends BaseEntity {

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private long size;

  public BinaryContent(String fileName, String contentType, long size) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
  }
}
