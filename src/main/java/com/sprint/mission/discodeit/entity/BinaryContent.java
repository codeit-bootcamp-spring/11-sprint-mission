package com.sprint.mission.discodeit.entity;


import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "binary_contents")
@Getter
@NoArgsConstructor
public class BinaryContent extends BaseEntity {

  @Column(nullable = false, length = 255, updatable = false)
  private String fileName;
  @Column(nullable = false, length = 100, updatable = false)
  private String contentType;
  @Column(nullable = false, updatable = false)
  private Long size;


  public BinaryContent(String fileName, String contentType, Long size) {
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
  }


}

