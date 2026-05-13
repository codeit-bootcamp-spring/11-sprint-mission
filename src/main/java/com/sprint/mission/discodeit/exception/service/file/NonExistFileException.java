package com.sprint.mission.discodeit.exception.service.file;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.FileException;
import java.util.Map;
import java.util.UUID;

public class NonExistFileException extends FileException {

  public NonExistFileException(UUID fileId) {
    super(ErrorCode.FILE_NOT_FOUND, Map.of("fileId", fileId));
  }
}
