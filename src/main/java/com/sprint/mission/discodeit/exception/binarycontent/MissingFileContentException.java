package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class MissingFileContentException extends BinaryContentException {

  public MissingFileContentException() {
    super(ErrorCode.MISSING_FILE_CONTENT, null);
  }
}
