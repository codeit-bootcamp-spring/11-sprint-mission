package com.sprint.mission.discodeit.exception;

import java.util.UUID;

public class DiscodeitIdMismatchException extends DiscodeitInvalidInputException {

  public DiscodeitIdMismatchException(String message) {
    super(message);
  }

  public static DiscodeitIdMismatchException channel(UUID pathId, UUID bodyId) {
    return new DiscodeitIdMismatchException(
        "channelId 불일치: pathId=" + pathId + ", bodyId=" + bodyId
    );
  }

  public static DiscodeitIdMismatchException message(UUID pathId, UUID bodyId) {
    return new DiscodeitIdMismatchException(
        "messageId 불일치: pathId=" + pathId + ", bodyId=" + bodyId
    );
  }

  public static DiscodeitIdMismatchException generic(String idName, UUID pathId, UUID bodyId) {
    return new DiscodeitIdMismatchException(
        idName + " 불일치: pathId=" + pathId + ", bodyId=" + bodyId
    );
  }
}
