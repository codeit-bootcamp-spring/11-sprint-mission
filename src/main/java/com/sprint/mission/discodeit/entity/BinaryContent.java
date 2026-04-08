package com.sprint.mission.discodeit.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sprint.mission.discodeit.entity.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

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

  @JsonIgnore
  @Column(nullable = false)
  private byte[] bytes;

  public BinaryContent(byte[] bytes, String fileName, String contentType) {
    this.bytes = bytes;
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = bytes != null ? bytes.length : 0;
  }
}
