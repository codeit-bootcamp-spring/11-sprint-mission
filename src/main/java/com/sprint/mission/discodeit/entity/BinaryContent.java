package com.sprint.mission.discodeit.entity;

import lombok.Getter;

import java.time.Instant;
import java.util.UUID;
import java.io.Serializable;

@Getter
public class BinaryContent implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID id;
  private Instant createdAt;
  private byte[] bytes;
  private String fileName;
  private Long size;
  private String contentType;

  public BinaryContent(byte[] bytes, String fileName,
      Long size, String contentType) {
    this.id = UUID.randomUUID();
    this.createdAt = Instant.now();
    this.bytes = bytes;
    this.fileName = fileName;
    this.size = size;
    this.contentType = contentType;
  }
}
