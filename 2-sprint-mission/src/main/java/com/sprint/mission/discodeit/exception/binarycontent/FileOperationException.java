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

  // 파일 저장 실패
  public static FileOperationException saveFailed() {
    return new FileOperationException(ErrorCode.FILE_SAVE_FAILED);
  }

  // 디렉토리 생성 실패
  public static FileOperationException directoryCreationFailed() {
    return new FileOperationException(ErrorCode.FILE_DIRECTORY_CREATION_FAILED);
  }

  // 파일 삭제 실패
  public static FileOperationException deleteFailed() {
    return new FileOperationException(ErrorCode.FILE_DELETE_FAILED);
  }

  // 파일 이미 존재함
  public static FileOperationException alreadyExists() {
    return new FileOperationException(ErrorCode.FILE_ALREADY_EXISTS);
  }
}