package com.sprint.mission.discodeit.entity;

import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;
import lombok.Getter;

@Getter
public class BinaryContent implements Serializable, Identifiable {

  private static final long serialVersionUID = 1L;
  private final UUID id;
  private final Instant createdAt;
  private final byte[] data;
  private final String fileName;
  private final String contentType;
  private final long size;

  public BinaryContent(byte[] data, String fileName, String contentType, long size) {
    this.id = UUID.randomUUID();
    this.createdAt = Instant.now();
    this.data = data;
    this.fileName = fileName;
    this.contentType = contentType;
    this.size = size;
  }
}
