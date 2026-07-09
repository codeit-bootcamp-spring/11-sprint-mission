package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseEntity;
import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Entity
@Table(name = "binary_contents")
@NoArgsConstructor
public class BinaryContent extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private String contentType;

  @Column(nullable = false)
  private long size;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BinaryContentStatus status = BinaryContentStatus.PROCESSING;

  public BinaryContent(String fileName, String contentType, long size) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
    this.status = BinaryContentStatus.PROCESSING;
  }

  public void updateStatus(BinaryContentStatus status) {
    this.status = status;
  }
}
