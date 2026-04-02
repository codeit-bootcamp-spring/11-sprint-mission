package com.sprint.mission.discodeit.entity;

import lombok.*;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class BinaryContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private Instant createdAt;

  private String fileName;
  private String contentType;
  private long size;

  private transient byte[] data;

  public BinaryContent(byte[] data, String fileName, String contentType) {
    this.id = UUID.randomUUID();
    this.createdAt = Instant.now();
    this.data = data;
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = data != null ? data.length : 0;
  }

  public String getBytes() {
    if (data == null) {
      return null;
    }
    return java.util.Base64.getEncoder().encodeToString(data);
  }
}
