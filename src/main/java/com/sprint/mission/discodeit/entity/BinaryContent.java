package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseUpdatableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_content")
@Getter
@NoArgsConstructor
public class BinaryContent extends BaseUpdatableEntity {

  @Column(nullable = false, length = 255, updatable = false)
  private String fileName;
  @Column(nullable = false, length = 100, updatable = false)
  private String contentType;
  @Column(nullable = false, updatable = false)
  private Long size;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private BinaryContentStatus status;


  public BinaryContent(String fileName, String contentType, Long size) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
    this.status = BinaryContentStatus.PROCESSING;
  }

  public void updateBinaryContentStatus(BinaryContentStatus status) {
    this.status = status;
  }

  public enum BinaryContentStatus {
    PROCESSING,
    SUCCESS,
    FAIL
  }


}

