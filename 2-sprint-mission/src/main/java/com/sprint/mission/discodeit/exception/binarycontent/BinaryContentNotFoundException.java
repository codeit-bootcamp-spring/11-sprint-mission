package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import com.sprint.mission.discodeit.exception.ErrorKey;
import java.util.UUID;

public class BinaryContentNotFoundException extends BinaryContentException {

  private BinaryContentNotFoundException() {
    super(ErrorCode.BINARY_CONTENT_NOT_FOUND);
  }

  public static BinaryContentNotFoundException withId(UUID binaryContentId) {
    BinaryContentNotFoundException exception = new BinaryContentNotFoundException();
    exception.addDetail(ErrorKey.BINARY_CONTENT_ID, binaryContentId);
    return exception;
  }
}
