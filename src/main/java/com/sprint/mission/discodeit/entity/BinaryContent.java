package com.sprint.mission.discodeit.entity;

import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BinaryContent extends BaseUpdatableEntity {

  @Column(nullable = false)
  private String fileName;

  @Column(nullable = false)
  private Long size;

  @Column(length = 100, nullable = false)
  private String contentType;

  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private BinaryContentStatus status = BinaryContentStatus.PROCESSING;

  public BinaryContent(String fileName, Long size, String contentType) {
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
  }

  public void updateStatus(BinaryContentStatus status) {
    // 바이너리 저장 결과에 따라 업로드 상태 변경함
    if (status != null && this.status != status) {
      this.status = status;
    }
  }
}
