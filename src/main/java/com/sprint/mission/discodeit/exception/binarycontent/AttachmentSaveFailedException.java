package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;
import java.util.Map;

public class AttachmentSaveFailedException extends BinaryContentException {

  public AttachmentSaveFailedException(String fileName) {
    super(ErrorCode.ATTACHMENT_SAVE_FAILED, Map.of("fileName", fileName));
  }
}
