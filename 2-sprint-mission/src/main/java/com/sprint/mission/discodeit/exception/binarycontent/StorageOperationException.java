package com.sprint.mission.discodeit.exception.binarycontent;

import com.sprint.mission.discodeit.exception.ErrorCode;

public class StorageOperationException extends BinaryContentException {

  private StorageOperationException(ErrorCode errorCode) {
    super(errorCode);
  }

  public static StorageOperationException uploadFailed() {
    return new StorageOperationException(ErrorCode.FILE_UPLOAD_FAILED);
  }

  public static StorageOperationException readFailed() {
    return new StorageOperationException(ErrorCode.FILE_READ_FAILED);
  }

  public static StorageOperationException deleteFailed() {
    return new StorageOperationException(ErrorCode.FILE_DELETE_FAILED);
  }

  public static StorageOperationException presignedUrlGenerationFailed() {
    return new StorageOperationException(ErrorCode.PRESIGNED_URL_GENERATION_FAILED);
  }
}