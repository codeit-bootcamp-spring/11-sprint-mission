package com.sprint.mission.discodeit.exception.file;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class FileEmptyException extends FileException {

  public FileEmptyException(String fileName) {
    super(ErrorCode.FILE_EMPTY, Map.of("fileName", fileName));
  }
}
