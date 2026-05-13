package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;
import java.util.UUID;

public class BinaryContentNotFoundException extends BinaryContentException {

  private BinaryContentNotFoundException(ErrorCode errorCode, Map<String, Object> details) {
    super(errorCode, details);
  }

  public static BinaryContentNotFoundException withId(UUID binaryContentId) {
    return new BinaryContentNotFoundException(
        ErrorCode.BINARY_CONTENT_NOT_FOUND,
        Map.of(
            "binaryContentId", binaryContentId
        )
    );
  }
}