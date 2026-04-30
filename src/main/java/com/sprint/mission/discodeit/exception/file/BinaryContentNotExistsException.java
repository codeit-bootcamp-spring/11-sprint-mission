package com.sprint.mission.discodeit.exception.file;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class BinaryContentNotExistsException extends FileException {

  public BinaryContentNotExistsException(UUID fileId) {
    super(ErrorCode.BINARY_CONTENT_NOT_EXISTS, Map.of("fileId", fileId));
  }
}
