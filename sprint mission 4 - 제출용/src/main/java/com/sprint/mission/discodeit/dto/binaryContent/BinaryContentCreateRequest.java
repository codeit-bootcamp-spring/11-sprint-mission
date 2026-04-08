package com.sprint.mission.discodeit.dto.binaryContent;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class BinaryContentCreateRequest {

  private UUID userId;
  private UUID messageId;
  private String fileName;
  private byte[] content;
  private String contentType;
}
