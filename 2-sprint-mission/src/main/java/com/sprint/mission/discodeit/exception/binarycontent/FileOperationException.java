package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class FileOperationException extends BinaryContentException {

  private FileOperationException(ErrorCode errorCode) {
    super(errorCode);
  }

  // 파일 읽기 실패
  public static FileOperationException readFailed() {
    return new FileOperationException(ErrorCode.FILE_READ_FAILED);
  }
}